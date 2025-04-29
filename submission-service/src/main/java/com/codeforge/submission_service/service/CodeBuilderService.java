package com.codeforge.submission_service.service;

import com.codeforge.submission_service.dto.ProblemDetailDto;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class CodeBuilderService {

    public String buildFullCode(String userCodeBody, ProblemDetailDto problem) {
        String returnType = problem.getReturnType();           // VD: "int[]"
        String methodName = problem.getMethodName();           // VD: "twoSum"
        String methodSignature = problem.getMethodSignature(); // VD: "int[] nums, int target"

        StringBuilder solutionCode = new StringBuilder();
        solutionCode.append("import java.util.*;\n\n");
//        if (returnType.contains("ListNode") || methodSignature.contains("ListNode")) {
//            solutionCode.append("class ListNode {\n");
//            solutionCode.append("    int val;\n");
//            solutionCode.append("    ListNode next;\n");
//            solutionCode.append("    ListNode() {}\n");
//            solutionCode.append("    ListNode(int val) { this.val = val; }\n");
//            solutionCode.append("    ListNode(int val, ListNode next) { this.val = val; this.next = next; }\n");
//            solutionCode.append("}\n\n");
//        }

        solutionCode.append("public class Solution {\n");
        solutionCode.append("    public ").append(methodSignature).append(" ").append(methodName)
                .append("(").append(returnType).append(") {\n");
        solutionCode.append(userCodeBody).append("\n");
        solutionCode.append("    }\n");
        solutionCode.append("}\n");

        return solutionCode.toString();
    }
    public String buildFullCodeC(String userCodeBody, ProblemDetailDto problem) {
        String methodName = problem.getMethodName();           // "twoSum"
        String javaParams = problem.getReturnType();           // "int[] nums, int target"
        String returnType = problem.getMethodSignature();      // "int[]"

        // Chuyển đổi methodSignature và tham số thành hàm C chuẩn
        String cFunctionSignature = convertJavaSignatureToC(methodName, returnType, javaParams);
        boolean usesListNode = userCodeBody.contains("ListNode") ||
                returnType.contains("ListNode") ||
                javaParams.contains("ListNode");
        String listNodeDefinition = usesListNode ? """
        struct ListNode {
            int val;
            struct ListNode* next;
        };
        """ : "";
        return """
#pragma GCC optimize("no-stack-protector")
#include <stdio.h>
#include <stdlib.h>
#include <limits.h>
#include <ctype.h>
#include <string.h>
#include <stdbool.h>
%s
%s {
%s
}
""".formatted(
                listNodeDefinition,
                cFunctionSignature,
                indentCCode(userCodeBody)
        );

    }

//    public String convertJavaSignatureToC(String methodName, String returnType, String javaParams) {
//        StringBuilder signature = new StringBuilder();
//
//        // Convert kiểu trả về
//        String cReturnType = returnType.contains("[]") ? "int*" : returnType;
//        signature.append(cReturnType).append(" ").append(methodName).append("(");
//
//        // Xử lý danh sách tham số
//        String[] params = javaParams.split(",");
//        List<String> cParams = new ArrayList<>();
//
//        for (String param : params) {
//            param = param.trim(); // e.g., "int[] nums"
//            if (param.contains("[]")) {
//                String[] parts = param.replace("[]", "").trim().split(" ");
//                String varName = parts[1];
//                cParams.add("int* " + varName);
//                cParams.add("int " + varName + "Size");
//            } else {
//                cParams.add(param);
//            }
//        }
//
//        if (returnType.contains("[]")) {
//            cParams.add("int* returnSize");
//        }
//
//        signature.append(String.join(", ", cParams)).append(")");
//        return signature.toString();
//    }
public String convertJavaSignatureToC(String methodName, String returnType, String javaParams) {
    StringBuilder signature = new StringBuilder();

    // Xử lý kiểu trả về
    String cReturnType;
    boolean is2DArrayReturn = returnType.contains("List<List<Integer>>") || returnType.contains("int[][]");

    if (is2DArrayReturn) {
        cReturnType = "int**";
    } else if (returnType.contains("[]")) {
        cReturnType = "int*";
    }else if (returnType.contains("ListNode")) {
        cReturnType = "struct ListNode*";
    }else if (returnType.equals("String")) {
        cReturnType = "char*";
    }else if (returnType.equals("boolean")) {
        cReturnType = "bool";
        }else {
        cReturnType = returnType; // e.g., int, double, etc.
    }

    signature.append(cReturnType).append(" ").append(methodName).append("(");

    // Xử lý tham số đầu vào
    String[] params = javaParams.split(",");
    List<String> cParams = new ArrayList<>();

    for (String param : params) {
        param = param.trim(); // e.g., "int[] nums"
        if (param.isEmpty()) continue;

        if (param.contains("[]")) {
            String[] parts = param.replace("[]", "").trim().split(" ");
            String varName = parts[1];
            cParams.add("int* " + varName);
            cParams.add("int " + varName + "Size");
        }else if (param.contains("ListNode")) {
            String varName = param.split(" ")[1];
            cParams.add("struct ListNode* " + varName);
            }else if (param.startsWith("String")) {
            String varName = param.split(" ")[1];
            cParams.add("char* " + varName);
        }else if (param.startsWith("boolean")) {
            String varName = param.split(" ")[1];
            cParams.add("bool " + varName);
        }else {
            cParams.add(param);
        }
    }

    // Nếu return là int** → cần thêm returnSize và returnColumnSizes
    if (is2DArrayReturn) {
        cParams.add("int* returnSize");
        cParams.add("int** returnColumnSizes");
    } else if (returnType.contains("[]")) {
        cParams.add("int* returnSize");
    }

    signature.append(String.join(", ", cParams)).append(")");
    return signature.toString();
}


    private String indentCCode(String body) {
        return Arrays.stream(body.split("\n"))
                .map(line -> "    " + line)
                .collect(Collectors.joining("\n"));
    }

// ... (import và class vẫn như cũ)

    // --- C++ ---
//    public String buildFullCodeCpp(String userCodeBody, ProblemDetailDto problem) {
//        String returnType = convertToCppType(problem.getMethodSignature());
//        String methodName = problem.getMethodName();
//        String params = convertJavaParamsToCpp(problem.getReturnType());
//
//        return """
//            #include <iostream>
//            #include <vector>
//            #include <climits>
//            #include <algorithm>
//            using namespace std;
//
//            class Solution {
//            public:
//                %s %s(%s) {
//                    %s
//                }
//            };
//            """.formatted(returnType, methodName, params, indentCpp(userCodeBody));
//    }

//    private String convertToCppType(String javaType) {
//        return javaType.replace("int[]", "vector<int>").replace("String", "string");
//    }
public String buildFullCodeCpp(String userCodeBody, ProblemDetailDto problem) {
    String returnType = convertToCppType(problem.getMethodSignature());
    String methodName = problem.getMethodName();
    String params = convertJavaParamsToCpp(problem.getReturnType());

    StringBuilder code = new StringBuilder();

    // Check if ListNode is used in signature or return type
    boolean usesListNode = returnType.contains("ListNode") || params.contains("ListNode");
    if (usesListNode) {
        code.append("struct ListNode {\n")
                .append("    int val;\n")
                .append("    ListNode* next;\n")
                .append("    ListNode(int x) : val(x), next(nullptr) {}\n")
                .append("};\n\n");
    }

    code.append("#include <iostream>\n")
            .append("#include <vector>\n")
            .append("#include <climits>\n")
            .append("#include <algorithm>\n")
            .append("#include <stack>\n")
            .append("#include <unordered_map>\n")
            .append("using namespace std;\n\n")
            .append("class Solution {\n")
            .append("public:\n")
            .append("    %s %s(%s) {\n".formatted(returnType, methodName, params))
            .append(indentCpp(userCodeBody)).append("\n")
            .append("    }\n")
            .append("};\n");

    return code.toString();
}

    private String convertToCppType(String javaType) {
    return javaType
            .replace("List<List<Integer>>", "vector<vector<int>>")
            .replace("List<Integer>", "vector<int>")
            .replace("int[]", "vector<int>")
            .replace("String", "string")
            .replace("boolean", "bool")
            .replace("ListNode", "ListNode*")
            .trim();
}
//    private String convertJavaParamsToCpp(String javaParams) {
//        return Arrays.stream(javaParams.split(","))
//                .map(p -> p.replace("int[]", "vector<int>").replace("String", "string"))
//                .collect(Collectors.joining(", "));
//    }
private String convertJavaParamsToCpp(String javaParams) {
    return Arrays.stream(javaParams.split(","))
            .map(p -> p.trim()
                    .replace("List<List<Integer>>", "vector<vector<int>>")
                    .replace("List<Integer>", "vector<int>")
                    .replace("int[]", "vector<int>")
                    .replace("ListNode", "ListNode*")
                    .replace("boolean", "bool")
                    .replace("String", "string"))
            .collect(Collectors.joining(", "));
}

    private String indentCpp(String body) {
        return Arrays.stream(body.split("\n"))
                .map(line -> "        " + line)
                .collect(Collectors.joining("\n"));
    }

    // --- Python ---
    public String buildFullCodePython(String userCodeBody, ProblemDetailDto problem) {
        String methodName = problem.getMethodName();
        String params = convertPythonParams(problem.getReturnType());

        return """
        class ListNode:
            def __init__(self, val=0, next=None):
                self.val = val
                self.next = next

        def %s(%s):
        %s
        """.formatted(methodName, params, indentPython(userCodeBody));
    }

//    private String convertPythonParams(String javaParams) {
//        return Arrays.stream(javaParams.split(","))
//                .map(p -> {
//                    p = p.trim();
//                    if (p.contains("[]")) return "nums";
//                    else return "target";
//                })
//                .collect(Collectors.joining(", "));
//    }
private String convertPythonParams(String javaParams) {
    return Arrays.stream(javaParams.split(","))
            .map(p -> {
                p = p.trim();
                String[] parts = p.split(" ");
                return parts[parts.length - 1]; // Lấy tên biến cuối cùng
            })
            .collect(Collectors.joining(", "));
}



    private String indentPython(String body) {
        return Arrays.stream(body.split("\n"))
                .map(line -> "        " + line)  // Python indent: 4 spaces under function
                .collect(Collectors.joining("\n"));
    }

    // --- C# ---
    public String buildFullCodeCSharp(String userCodeBody, ProblemDetailDto problem) {
        String returnType = convertToCSharpType(problem.getMethodSignature());
        String methodName = problem.getMethodName();
        String parameters = convertJavaParamsToCSharp(problem.getReturnType());

        return """
            using System;

            public class Solution {
                public %s %s(%s) {
                    %s
                }
            }
            """.formatted(returnType, methodName, parameters, indentCCode(userCodeBody));
    }

    private String convertToCSharpType(String javaType) {
        return javaType.replace("int[]", "int[]").replace("String", "string");
    }

    private String convertJavaParamsToCSharp(String javaParams) {
        return Arrays.stream(javaParams.split(","))
                .map(p -> p.replace("int[]", "int[]").replace("String", "string").trim())
                .collect(Collectors.joining(", "));
    }


}
