package com.codeforge.judge_service.runer;

import com.codeforge.judge_service.dto.JudgeRequest;
import com.codeforge.judge_service.dto.JudgeResponse;

import java.io.File;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.TimeUnit;

public class DockerRunner {

    public static JudgeResponse run(JudgeRequest req) throws Exception {
        Path tempRoot = Paths.get("/tmp/docker_tmp");
       // Path tempRoot = Paths.get("C:/docker_tmp");

        if (!Files.exists(tempRoot)) Files.createDirectories(tempRoot);
        Path tempDir = Files.createTempDirectory(tempRoot, "judge_");

        String language = req.getLanguage().toLowerCase();
        if (language.equals("java") && (req.getSolutionCode() == null || req.getMainCode() == null)) {
            return new JudgeResponse("ERROR", "", "Missing Java code to compile",0);
        }
        if ((language.equals("c") || language.equals("cpp") || language.equals("c++")) &&
                (req.getSolutionCode() == null || req.getMainCode() == null)) {
            return new JudgeResponse("ERROR", "", "Missing C/C++ code to compile",0);
        }

        String image;
        String runCommand;

        switch (language) {
            case "java" -> {
                image = "openjdk:17";
                runCommand = "javac Main.java Solution.java && java Main < input.txt > output.txt";
                File solutionFile = new File(tempDir.toFile(), "Solution.java");
                File mainFile = new File(tempDir.toFile(), "Main.java");
                Files.write(solutionFile.toPath(), req.getSolutionCode().getBytes(StandardCharsets.UTF_8));
                Files.write(mainFile.toPath(), req.getMainCode().getBytes(StandardCharsets.UTF_8));
            }
            case "c" -> {
                image = "gcc:latest";
                runCommand = "gcc solution.c main.c -o solution && ./solution < input.txt > output.txt";
                File cFile = new File(tempDir.toFile(), "solution.c");
                File mainFile = new File(tempDir.toFile(), "main.c");
                Files.write(cFile.toPath(), req.getSolutionCode().getBytes(StandardCharsets.UTF_8));
                Files.write(mainFile.toPath(), req.getMainCode().getBytes(StandardCharsets.UTF_8));
            }
            case "cpp", "c++" -> {
                image = "gcc:latest";
                runCommand = "g++ solution.cpp main.cpp -o solution && ./solution < input.txt > output.txt";
                File cppFile = new File(tempDir.toFile(), "solution.cpp");
                File mainFile = new File(tempDir.toFile(), "main.cpp");
                Files.write(cppFile.toPath(), req.getSolutionCode().getBytes(StandardCharsets.UTF_8));
                Files.write(mainFile.toPath(), req.getMainCode().getBytes(StandardCharsets.UTF_8));
            }
            case "python", "py" -> {
                image = "python:3.10";
                runCommand = "python3 main.py < input.txt > output.txt";

                File pyMainFile = new File(tempDir.toFile(), "main.py");
                File pySolutionFile = new File(tempDir.toFile(), "solution.py");

                // 🧠 mainCode là file main.py
                // 🧠 solutionCode là file solution.py
                Files.write(pyMainFile.toPath(), req.getMainCode().getBytes(StandardCharsets.UTF_8));
                Files.write(pySolutionFile.toPath(), req.getSolutionCode().getBytes(StandardCharsets.UTF_8));
            }
            case "csharp", "c#" -> {
                image = "mcr.microsoft.com/dotnet/sdk:7.0";
                runCommand = "dotnet new console -n JudgeApp -o app && cd app && echo \"" +
                        req.getSolutionCode().replace("\"", "\\\"") + "\" > Solution.cs && echo \"" +
                        req.getMainCode().replace("\"", "\\\"") + "\" >> Program.cs && dotnet run < ../input.txt > ../output.txt";
                // tạo file input.txt như bình thường, phần build/run dotnet sẽ xử lý bên trong container
            }
            default -> throw new IllegalArgumentException("Unsupported language: " + req.getLanguage());
        }

        // Write input file
        File inputFile = new File(tempDir.toFile(), "input.txt");
        Files.write(inputFile.toPath(), req.getInput().getBytes(StandardCharsets.UTF_8));
        Thread.sleep(300); // Wait for Docker

        // Mount path
        String rawPath = tempDir.toRealPath().toString().replace("\\", "/");
        String mountPath = System.getProperty("os.name").toLowerCase().contains("win")
                ? "/host_mnt/" + rawPath.substring(0, 1).toLowerCase() + rawPath.substring(2)
                : rawPath;

        System.out.println("\uD83D\uDC49 Mount path: " + mountPath);

        ProcessBuilder pb = new ProcessBuilder(
                "docker", "run", "--rm",
                "--platform", "linux/amd64",
                "-v", mountPath + ":/app",
                "-w", "/app",
                image,
                "bash", "-c", runCommand
        );

        pb.redirectErrorStream(true);
        System.out.println("\uD83D\uDC49 Docker command: " + String.join(" ", pb.command()));

        long startTime = System.nanoTime(); // 🔥 Bắt đầu đo
        Process p = pb.start();
        boolean finished = p.waitFor(5, TimeUnit.SECONDS);
        long endTime = System.nanoTime(); // 🔥 Kết thúc đo

        long executionTimeMs = ((endTime - startTime) / 1_000_000); // Chia nano -> milli
        System.out.println("👉 Execution time: " + executionTimeMs + " ms"); // In thời gian ra log

        if (!finished) {
            p.destroyForcibly();
            return new JudgeResponse("TLE", "", "Time Limit Exceeded",0);
        }

        InputStream processOutput = p.getInputStream();
        String logs = new String(processOutput.readAllBytes());

        File outputFile = new File(tempDir.toFile(), "output.txt");
        String output = outputFile.exists() ? Files.readString(outputFile.toPath()).trim() : "";
        String expected = req.getExpectedOutput().trim();

        // ✅ Normalize output và expected để tránh lỗi so sánh do khoảng trắng
        String normalizedOutput = output.replaceAll("\\s+", "");
        String normalizedExpected = expected.replaceAll("\\s+", "");

        String status = (p.exitValue() == 0 && normalizedOutput.equals(normalizedExpected)) ? "PASS" : "FAIL";

        return new JudgeResponse(status, output, logs,executionTimeMs);
    }
}
//package com.codeforge.judge_service.runer;
//
//import com.codeforge.judge_service.dto.JudgeRequest;
//import com.codeforge.judge_service.dto.JudgeResponse;
//
//import java.io.File;
//import java.io.InputStream;
//import java.nio.charset.StandardCharsets;
//import java.nio.file.Files;
//import java.nio.file.Path;
//import java.nio.file.Paths;
//import java.util.concurrent.TimeUnit;
//
//public class DockerRunner {
//
//    public static JudgeResponse run(JudgeRequest req) throws Exception {
//        Path tempRoot = Paths.get("C:/docker_tmp");
//        if (!Files.exists(tempRoot)) Files.createDirectories(tempRoot);
//        Path tempDir = Files.createTempDirectory(tempRoot, "judge_");
//
//        String language = req.getLanguage().toLowerCase();
//        if (language.equals("java") && (req.getSolutionCode() == null || req.getMainCode() == null)) {
//            return new JudgeResponse("ERROR", "", "Missing Java code to compile");
//        }
//        if ((language.equals("c") || language.equals("cpp") || language.equals("c++")) &&
//                (req.getSolutionCode() == null || req.getMainCode() == null)) {
//            return new JudgeResponse("ERROR", "", "Missing C/C++ code to compile");
//        }
//
//        String image;
//        String runCommand;
//
//        switch (language) {
//            case "java" -> {
//                image = "openjdk:17";
//                runCommand = "javac Main.java Solution.java && java Main < input.txt > output.txt";
//                File solutionFile = new File(tempDir.toFile(), "Solution.java");
//                File mainFile = new File(tempDir.toFile(), "Main.java");
//                Files.write(solutionFile.toPath(), req.getSolutionCode().getBytes(StandardCharsets.UTF_8));
//                Files.write(mainFile.toPath(), req.getMainCode().getBytes(StandardCharsets.UTF_8));
//            }
//            case "c" -> {
//                image = "gcc:latest";
//                runCommand = "gcc solution.c main.c -o solution && ./solution < input.txt > output.txt";
//                File cFile = new File(tempDir.toFile(), "solution.c");
//                File mainFile = new File(tempDir.toFile(), "main.c");
//                Files.write(cFile.toPath(), req.getSolutionCode().getBytes(StandardCharsets.UTF_8));
//                Files.write(mainFile.toPath(), req.getMainCode().getBytes(StandardCharsets.UTF_8));
//            }
//            case "cpp", "c++" -> {
//                image = "gcc:latest";
//                runCommand = "g++ solution.cpp main.cpp -o solution && ./solution < input.txt > output.txt";
//                File cppFile = new File(tempDir.toFile(), "solution.cpp");
//                File mainFile = new File(tempDir.toFile(), "main.cpp");
//                Files.write(cppFile.toPath(), req.getSolutionCode().getBytes(StandardCharsets.UTF_8));
//                Files.write(mainFile.toPath(), req.getMainCode().getBytes(StandardCharsets.UTF_8));
//            }
//            default -> throw new IllegalArgumentException("Unsupported language: " + req.getLanguage());
//        }
//
//        // Write input file
//        File inputFile = new File(tempDir.toFile(), "input.txt");
//        Files.write(inputFile.toPath(), req.getInput().getBytes(StandardCharsets.UTF_8));
//        Thread.sleep(300); // Wait for Docker
//
//        // Mount path (no need to change on Linux VPS)
//        String rawPath = tempDir.toRealPath().toString().replace("\\", "/");
//        String mountPath = rawPath;
//
//        System.out.println("👉 Mount path: " + mountPath);
//
//        ProcessBuilder pb = new ProcessBuilder(
//                "docker", "run", "--rm",
//                "--platform", "linux/amd64",
//                "-v", mountPath + ":/app",
//                "-w", "/app",
//                image,
//                "bash", "-c", runCommand
//        );
//
//        pb.redirectErrorStream(true);
//        System.out.println("👉 Docker command: " + String.join(" ", pb.command()));
//
//        Process p = pb.start();
//        boolean finished = p.waitFor(5, TimeUnit.SECONDS);
//        if (!finished) {
//            p.destroyForcibly();
//            return new JudgeResponse("TLE", "", "Time Limit Exceeded");
//        }
//
//        InputStream processOutput = p.getInputStream();
//        String logs = new String(processOutput.readAllBytes());
//
//        File outputFile = new File(tempDir.toFile(), "output.txt");
//        String output = outputFile.exists() ? Files.readString(outputFile.toPath()).trim() : "";
//        String expected = req.getExpectedOutput().trim();
//
//        // ✅ Normalize output và expected để tránh lỗi so sánh do khoảng trắng
//        String normalizedOutput = output.replaceAll("\\s+", "");
//        String normalizedExpected = expected.replaceAll("\\s+", "");
//
//        String status = (p.exitValue() == 0 && normalizedOutput.equals(normalizedExpected)) ? "PASS" : "FAIL";
//
//        return new JudgeResponse(status, output, logs);
//    }
//}
