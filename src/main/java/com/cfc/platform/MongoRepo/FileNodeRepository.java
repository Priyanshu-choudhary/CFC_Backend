package com.cfc.platform.MongoRepo;

import com.cfc.platform.Pojo.FileNode.FileNode;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface FileNodeRepository extends MongoRepository<FileNode, String> {

    Optional<FileNode> findByIdAndUserId(String id, String userId);

    List<FileNode> findByUserId(String userId);

    List<FileNode> findByUserId(String userId, Sort sort);

    List<FileNode> findByUserIdAndParentId(String userId, String parentId, Sort sort);

    List<FileNode> findByUserIdAndParentIdIsNull(String userId, Sort sort);

    boolean existsByUserIdAndParentIdAndNameIgnoreCase(String userId, String parentId, String name);

    boolean existsByUserIdAndParentIdAndNameIgnoreCaseAndIdNot(String userId, String parentId, String name, String id);

    boolean existsByUserIdAndParentIdIsNullAndNameIgnoreCase(String userId, String name);

    boolean existsByUserIdAndParentIdIsNullAndNameIgnoreCaseAndIdNot(String userId, String name, String id);

    List<FileNode> findByUserIdAndNameContainingIgnoreCase(String userId, String name, Sort sort);

    void deleteByUserIdAndIdIn(String userId, List<String> ids);
}
