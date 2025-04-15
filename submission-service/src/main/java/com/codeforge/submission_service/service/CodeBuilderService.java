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
        solutionCode.append("public class Solution {\n");
        solutionCode.append("    public ").append(methodSignature).append(" ").append(methodName)
                .append("(").append(returnType).append(") {\n");
        solutionCode.append(userCodeBody).append("\n");
        solutionCode.append("    }\n");
        solutionCode.append("}\n");

        return solutionCode.toString();
    }

//    public String buildMainJava(String methodName, String returnType, String methodSignature, String sampleInput) {
//        return """
//        import java.util.*;
//
//        public class Main {
//            public static void main(String[] args) {
//                Scanner sc = new Scanner(System.in);
//                String input = sc.nextLine();
//
//                try {
//                    int leftBracket = input.indexOf('[');
//                    int rightBracket = input.indexOf(']');
//                    if (leftBracket == -1 || rightBracket == -1 || rightBracket <= leftBracket) {
//                        throw new IllegalArgumentException("Input format invalid: missing brackets");
//                    }
//
//                    String numsStr = input.substring(leftBracket + 1, rightBracket);
//                    String[] numParts = numsStr.split(",");
//                    int[] nums = Arrays.stream(numParts)
//                                       .map(String::trim)
//                                       .mapToInt(Integer::parseInt)
//                                       .toArray();
//
//                    String afterBracket = input.substring(rightBracket + 1);
//                    int target = Integer.parseInt(afterBracket.replace(",", "").trim());
//
//                    Solution sol = new Solution();
//                    int[] result = sol.%s(nums, target);
//
//                    System.out.println(Arrays.toString(result));
//                } catch (Exception e) {
//                    System.out.println("⚠️ Input parsing error: " + e.getMessage());
//                }
//            }
//        }
//        """.formatted(methodName);
//    }

    public String buildFullCodeC(String userCodeBody, ProblemDetailDto problem) {
        String methodName = problem.getMethodName();           // "twoSum"
        String javaParams = problem.getReturnType();           // "int[] nums, int target"
        String returnType = problem.getMethodSignature();      // "int[]"

        // Chuyển đổi methodSignature và tham số thành hàm C chuẩn
        String cFunctionSignature = convertJavaSignatureToC(methodName, returnType, javaParams);

        return """
        #include <stdio.h>
        #include <stdlib.h>
        #include <limits.h>
        %s {
        %s
        }
        """.formatted(
                cFunctionSignature,
                indentCCode(userCodeBody)
        );
    }

    public String convertJavaSignatureToC(String methodName, String returnType, String javaParams) {
        StringBuilder signature = new StringBuilder();

        // Convert kiểu trả về
        String cReturnType = returnType.contains("[]") ? "int*" : returnType;
        signature.append(cReturnType).append(" ").append(methodName).append("(");

        // Xử lý danh sách tham số
        String[] params = javaParams.split(",");
        List<String> cParams = new ArrayList<>();

        for (String param : params) {
            param = param.trim(); // e.g., "int[] nums"
            if (param.contains("[]")) {
                String[] parts = param.replace("[]", "").trim().split(" ");
                String varName = parts[1];
                cParams.add("int* " + varName);
                cParams.add("int " + varName + "Size");
            } else {
                cParams.add(param);
            }
        }

        if (returnType.contains("[]")) {
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



//    public String buildMainC(String methodName) {
//        return """
//            #include <stdio.h>
//            #include <stdlib.h>
//
//            extern int* %s(int* nums, int numsSize, int target, int* returnSize);
//
//            int main() {
//                int nums[] = {2, 7, 11, 15};
//                int target = 9;
//                int returnSize;
//                int* result = %s(nums, 4, target, &returnSize);
//                printf("[%%d, %%d]", result[0], result[1]);
//                return 0;
//            }
//            """.formatted(methodName, methodName);
//    }
}
