package com.cfc.platform.Service;

import com.cfc.platform.DTO.CreateFileNodeRequest;
import com.cfc.platform.DTO.FileNodeResponse;
import com.cfc.platform.DTO.MoveFileNodeRequest;
import com.cfc.platform.DTO.UpdateFileNodeRequest;
import com.cfc.platform.MongoRepo.FileNodeRepository;
import com.cfc.platform.Pojo.FileNode.FileNode;
import com.cfc.platform.Pojo.User;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Objects;

@Service
public class FileNodeService {

    private static final Sort NODE_SORT = Sort.by(
            Sort.Order.asc("order"),
            Sort.Order.desc("folder"),
            Sort.Order.asc("name"),
            Sort.Order.asc("createdAt")
    );

    private final FileNodeRepository fileNodeRepository;
    private final UserService userService;

    public FileNodeService(FileNodeRepository fileNodeRepository, UserService userService) {
        this.fileNodeRepository = fileNodeRepository;
        this.userService = userService;
    }

    public FileNodeResponse createNode(String username, CreateFileNodeRequest request) {
        String userId = resolveUserId(username);
        validateCreateRequest(request);

        FileNode parent = getParentIfPresent(userId, request.getParentId());
        if (parent != null && !parent.isFolder()) {
            throw new IllegalArgumentException("Files cannot contain child nodes.");
        }

        ensureUniqueName(userId, request.getParentId(), request.getName(), null);

        Instant now = Instant.now();
        FileNode node = new FileNode();
        node.setUserId(userId);
        node.setParentId(normalizeParentId(request.getParentId()));
        node.setName(request.getName().trim());
        node.setFolder(Boolean.TRUE.equals(request.getFolder()));
        node.setContent(node.isFolder() ? null : defaultFileContent(request.getContent()));
        node.setOrder(resolveOrder(userId, request.getParentId(), request.getOrder()));
        node.setCreatedAt(now);
        node.setUpdatedAt(now);

        return toResponse(saveNode(node));
    }

    public List<FileNodeResponse> getTree(String username) {
        String userId = resolveUserId(username);
        List<FileNode> nodes = new ArrayList<>(fileNodeRepository.findByUserId(userId, NODE_SORT));
        nodes.sort(fileNodeComparator());

        Map<String, FileNodeResponse> responseById = new HashMap<>();
        List<FileNodeResponse> roots = new ArrayList<>();

        for (FileNode node : nodes) {
            responseById.put(node.getId(), toResponse(node));
        }

        for (FileNode node : nodes) {
            FileNodeResponse current = responseById.get(node.getId());
            if (node.getParentId() == null) {
                roots.add(current);
                continue;
            }

            FileNodeResponse parent = responseById.get(node.getParentId());
            if (parent == null) {
                roots.add(current);
                continue;
            }
            parent.getChildren().add(current);
        }

        sortResponseTree(roots);
        return roots;
    }

    public FileNodeResponse getNode(String username, String nodeId) {
        String userId = resolveUserId(username);
        return toResponse(getNodeOrThrow(userId, nodeId));
    }

    public FileNodeResponse updateNode(String username, String nodeId, UpdateFileNodeRequest request) {
        String userId = resolveUserId(username);
        if (request == null) {
            throw new IllegalArgumentException("Request body is required.");
        }

        FileNode node = getNodeOrThrow(userId, nodeId);
        boolean changed = false;

        if (request.getName() != null) {
            String newName = validateName(request.getName());
            ensureUniqueName(userId, node.getParentId(), newName, node.getId());
            node.setName(newName);
            changed = true;
        }

        if (request.getContent() != null) {
            if (node.isFolder()) {
                throw new IllegalArgumentException("Folder content cannot be updated.");
            }
            node.setContent(request.getContent());
            changed = true;
        }

        if (request.getOrder() != null) {
            validateOrder(request.getOrder());
            node.setOrder(request.getOrder());
            changed = true;
        }

        if (!changed) {
            throw new IllegalArgumentException("Nothing to update.");
        }

        node.setUpdatedAt(Instant.now());
        return toResponse(saveNode(node));
    }

    public FileNodeResponse moveNode(String username, String nodeId, MoveFileNodeRequest request) {
        String userId = resolveUserId(username);
        if (request == null) {
            throw new IllegalArgumentException("Request body is required.");
        }

        FileNode node = getNodeOrThrow(userId, nodeId);
        String newParentId = normalizeParentId(request.getNewParentId());
        FileNode newParent = getParentIfPresent(userId, newParentId);

        if (newParent != null && !newParent.isFolder()) {
            throw new IllegalArgumentException("Target parent must be a folder.");
        }

        if (Objects.equals(node.getId(), newParentId)) {
            throw new IllegalArgumentException("A node cannot be moved inside itself.");
        }

        if (node.isFolder() && newParentId != null) {
            assertNotMovingIntoDescendant(userId, node.getId(), newParentId);
        }

        ensureUniqueName(userId, newParentId, node.getName(), node.getId());

        boolean parentChanged = !Objects.equals(node.getParentId(), newParentId);
        node.setParentId(newParentId);
        if (request.getOrder() != null) {
            validateOrder(request.getOrder());
            node.setOrder(request.getOrder());
        } else if (parentChanged) {
            node.setOrder(resolveOrder(userId, newParentId, null));
        }
        node.setUpdatedAt(Instant.now());

        return toResponse(saveNode(node));
    }

    public void deleteNode(String username, String nodeId) {
        String userId = resolveUserId(username);
        getNodeOrThrow(userId, nodeId);

        List<FileNode> allNodes = fileNodeRepository.findByUserId(userId);
        Map<String, List<String>> childrenByParentId = new HashMap<>();
        for (FileNode node : allNodes) {
            String parentId = node.getParentId();
            if (parentId == null) {
                continue;
            }
            childrenByParentId.computeIfAbsent(parentId, key -> new ArrayList<>()).add(node.getId());
        }

        List<String> idsToDelete = new ArrayList<>();
        Deque<String> stack = new ArrayDeque<>();
        stack.push(nodeId);

        while (!stack.isEmpty()) {
            String current = stack.pop();
            idsToDelete.add(current);

            List<String> children = childrenByParentId.get(current);
            if (children == null) {
                continue;
            }
            for (String childId : children) {
                stack.push(childId);
            }
        }

        fileNodeRepository.deleteAllById(idsToDelete);
    }

    public List<FileNodeResponse> searchByName(String username, String query) {
        String userId = resolveUserId(username);
        if (query == null || query.trim().isEmpty()) {
            throw new IllegalArgumentException("Search query is required.");
        }

        List<FileNode> matches = fileNodeRepository.findByUserIdAndNameContainingIgnoreCase(
                userId,
                query.trim(),
                NODE_SORT
        );
        matches.sort(fileNodeComparator());

        List<FileNodeResponse> responses = new ArrayList<>();
        for (FileNode node : matches) {
            responses.add(toResponse(node));
        }
        return responses;
    }

    private String resolveUserId(String username) {
        User user = userService.findByName(username);
        if (user == null || user.getId() == null) {
            throw new NoSuchElementException("Authenticated user was not found.");
        }
        return user.getId();
    }

    private void validateCreateRequest(CreateFileNodeRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Request body is required.");
        }
        if (request.getFolder() == null) {
            throw new IllegalArgumentException("folder is required.");
        }

        validateName(request.getName());
        validateOrder(request.getOrder());

        if (Boolean.TRUE.equals(request.getFolder()) && request.getContent() != null && !request.getContent().isBlank()) {
            throw new IllegalArgumentException("Folders cannot store file content.");
        }
    }

    private String validateName(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Node name is required.");
        }
        String trimmed = name.trim();
        if (trimmed.length() > 255) {
            throw new IllegalArgumentException("Node name must be 255 characters or less.");
        }
        if (trimmed.contains("/")) {
            throw new IllegalArgumentException("Node name cannot contain '/'.");
        }
        return trimmed;
    }

    private void validateOrder(Integer order) {
        if (order != null && order < 0) {
            throw new IllegalArgumentException("order must be greater than or equal to 0.");
        }
    }

    private FileNode getParentIfPresent(String userId, String parentId) {
        String normalizedParentId = normalizeParentId(parentId);
        if (normalizedParentId == null) {
            return null;
        }

        return fileNodeRepository.findByIdAndUserId(normalizedParentId, userId)
                .orElseThrow(() -> new NoSuchElementException("Parent folder not found."));
    }

    private FileNode getNodeOrThrow(String userId, String nodeId) {
        return fileNodeRepository.findByIdAndUserId(nodeId, userId)
                .orElseThrow(() -> new NoSuchElementException("File node not found."));
    }

    private String normalizeParentId(String parentId) {
        if (parentId == null || parentId.trim().isEmpty()) {
            return null;
        }
        return parentId.trim();
    }

    private String defaultFileContent(String content) {
        return content == null ? "" : content;
    }

    private Integer resolveOrder(String userId, String parentId, Integer requestedOrder) {
        if (requestedOrder != null) {
            return requestedOrder;
        }

        List<FileNode> siblings = getSiblings(userId, parentId);
        int nextOrder = 0;
        for (FileNode sibling : siblings) {
            if (sibling.getOrder() != null) {
                nextOrder = Math.max(nextOrder, sibling.getOrder() + 1);
            }
        }
        return nextOrder;
    }

    private List<FileNode> getSiblings(String userId, String parentId) {
        String normalizedParentId = normalizeParentId(parentId);
        if (normalizedParentId == null) {
            return fileNodeRepository.findByUserIdAndParentIdIsNull(userId, NODE_SORT);
        }
        return fileNodeRepository.findByUserIdAndParentId(userId, normalizedParentId, NODE_SORT);
    }

    private void ensureUniqueName(String userId, String parentId, String name, String excludedId) {
        String normalizedParentId = normalizeParentId(parentId);
        boolean exists;

        if (normalizedParentId == null) {
            exists = excludedId == null
                    ? fileNodeRepository.existsByUserIdAndParentIdIsNullAndNameIgnoreCase(userId, name)
                    : fileNodeRepository.existsByUserIdAndParentIdIsNullAndNameIgnoreCaseAndIdNot(userId, name, excludedId);
        } else {
            exists = excludedId == null
                    ? fileNodeRepository.existsByUserIdAndParentIdAndNameIgnoreCase(userId, normalizedParentId, name)
                    : fileNodeRepository.existsByUserIdAndParentIdAndNameIgnoreCaseAndIdNot(userId, normalizedParentId, name, excludedId);
        }

        if (exists) {
            throw new IllegalStateException("A file or folder with this name already exists in the target folder.");
        }
    }

    private void assertNotMovingIntoDescendant(String userId, String nodeId, String newParentId) {
        List<FileNode> allNodes = fileNodeRepository.findByUserId(userId);
        Map<String, FileNode> nodeById = new HashMap<>();
        for (FileNode node : allNodes) {
            nodeById.put(node.getId(), node);
        }

        String cursor = newParentId;
        while (cursor != null) {
            if (Objects.equals(cursor, nodeId)) {
                throw new IllegalArgumentException("A folder cannot be moved inside one of its descendants.");
            }
            FileNode current = nodeById.get(cursor);
            cursor = current == null ? null : current.getParentId();
        }
    }

    private FileNodeResponse toResponse(FileNode node) {
        FileNodeResponse response = new FileNodeResponse();
        response.setId(node.getId());
        response.setName(node.getName());
        response.setFolder(node.isFolder());
        response.setParentId(node.getParentId());
        response.setContent(node.getContent());
        response.setOrder(node.getOrder());
        response.setCreatedAt(node.getCreatedAt());
        response.setUpdatedAt(node.getUpdatedAt());
        return response;
    }

    private FileNode saveNode(FileNode node) {
        try {
            return fileNodeRepository.save(node);
        } catch (DuplicateKeyException ex) {
            throw new IllegalStateException("A file or folder with this name already exists in the target folder.");
        }
    }

    private Comparator<FileNode> fileNodeComparator() {
        return Comparator
                .comparing((FileNode node) -> node.getOrder() == null ? Integer.MAX_VALUE : node.getOrder())
                .thenComparing(node -> !node.isFolder())
                .thenComparing(FileNode::getName, String.CASE_INSENSITIVE_ORDER)
                .thenComparing(FileNode::getCreatedAt, Comparator.nullsLast(Comparator.naturalOrder()));
    }

    private Comparator<FileNodeResponse> responseComparator() {
        return Comparator
                .comparing((FileNodeResponse node) -> node.getOrder() == null ? Integer.MAX_VALUE : node.getOrder())
                .thenComparing(node -> !node.isFolder())
                .thenComparing(FileNodeResponse::getName, String.CASE_INSENSITIVE_ORDER)
                .thenComparing(FileNodeResponse::getCreatedAt, Comparator.nullsLast(Comparator.naturalOrder()));
    }

    private void sortResponseTree(List<FileNodeResponse> nodes) {
        nodes.sort(responseComparator());
        for (FileNodeResponse node : nodes) {
            if (!node.getChildren().isEmpty()) {
                sortResponseTree(node.getChildren());
            }
        }
    }
}
