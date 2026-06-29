curl -X POST "http://localhost:9090/Posts/username/CFC-Team" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJDRkMtVGVhbSIsImlhdCI6MTc4MjY3NjYwMiwiZXhwIjoxNzgyNzYzMDAyfQ.W3HGHW-yKc9NRR-j5TusyIOjdjBAN1h-4AK-IvHMvec" \
  -d @- <<'EOF'
{
    "title": "Three Pairwise Maximums",
    "difficulty": "",
    "type": "coding",
    "accuracy": "",
    "tags": [
        "math",
        "800"
    ],
    "companies": [],
    "constrain": "",
    "description": "<div>\n <p>You are given three positive (i.e. strictly greater than zero) integers $$$x$$$, $$$y$$$ and $$$z$$$.</p>\n <p>Your task is to find positive integers $$$a$$$, $$$b$$$ and $$$c$$$ such that $$$x = \\max(a, b)$$$, $$$y = \\max(a, c)$$$ and $$$z = \\max(b, c)$$$, or determine that it is impossible to find such $$$a$$$, $$$b$$$ and $$$c$$$.</p>\n <p>You have to answer $$$t$$$ independent test cases. Print required $$$a$$$, $$$b$$$ and $$$c$$$ in any (arbitrary) order.</p>\n</div><div class=\"input-specification\">\n <div class=\"section-title\">\n  Input\n </div>\n <p>The first line of the input contains one integer $$$t$$$ ($$$1 \\le t \\le 2 \\cdot 10^4$$$) — the number of test cases. Then $$$t$$$ test cases follow.</p>\n <p>The only line of the test case contains three integers $$$x$$$, $$$y$$$, and $$$z$$$ ($$$1 \\le x, y, z \\le 10^9$$$).</p>\n</div><div class=\"output-specification\">\n <div class=\"section-title\">\n  Output\n </div>\n <p>For each test case, print the answer:</p>\n <ul>\n  <li>\"<span class=\"tex-font-style-tt\">NO</span>\" in the only line of the output if a solution doesn't exist;</li>\n  <li>or \"<span class=\"tex-font-style-tt\">YES</span>\" in the first line and <span class=\"tex-font-style-bf\">any</span> valid triple of positive integers $$$a$$$, $$$b$$$ and $$$c$$$ ($$$1 \\le a, b, c \\le 10^9$$$) in the second line. You can print $$$a$$$, $$$b$$$ and $$$c$$$ <span class=\"tex-font-style-bf\">in any order</span>.</li>\n </ul>\n</div>",
    "testcase": {
        "5\n3 2 3\n100 100 100\n50 49 49\n10 30 20\n1 1000000000 1000000000": "YES\n3 2 1\nYES\n100 100 100\nNO\nNO\nYES\n1 1 1000000000"
    },
    "codeTemplates": {},
    "timeLimitSeconds": 1,
    "memoryLimitKb": 262144
}
EOF