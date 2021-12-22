package com.vaadin.fusion.maven;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import com.vaadin.flow.server.ExecutionFailedException;
import com.vaadin.flow.server.frontend.TaskGenerateHilla;

public class TaskGenerateHillaImpl implements TaskGenerateHilla {

    @Override
    public void execute() throws ExecutionFailedException {
        List<String> command = prepareCommand();
        runCodeGeneration(command);
    }

    private void runCodeGeneration(List<String> command) throws ExecutionFailedException {
        var exitCode = 0;
        try {
            ProcessBuilder builder = new ProcessBuilder(command).inheritIO();
            exitCode = builder.start().waitFor();
        } catch (Exception e) {
            throw new ExecutionFailedException(
                    "Hilla Generator execution failed", e);
        }
        if (exitCode != 0) {
            throw new ExecutionFailedException(
                    "Hilla Generator execution failed with exit code " + exitCode);
        }
    }

    private boolean isMavenProject(Path path) {
        return path.resolve("pom.xml").toFile().exists();
    }

    private boolean isGradleProject(Path path) {
        return path.resolve("build.gradle").toFile().exists();
    }

    private List<String> prepareCommand() {
        String baseDirCandidate = System.getProperty("user.dir", ".");
        Path path = Paths.get(baseDirCandidate);
        if (path.toFile().isDirectory()) {
            if(isMavenProject(path)) {
                return prepareMavenCommand();
            }else if(isGradleProject(path)) {
                return prepareGradleCommand();
            }
        }
        throw new IllegalStateException(String.format(
                    "Failed to determine project directory for dev mode. "
                            + "Directory '%s' does not look like a Maven or "
                            + "Gradle project.",
                    path.toString())); 
    }

    private List<String> prepareMavenCommand() {
        List<String> command = new ArrayList<>();
        command.add("mvn");
        command.add("fusion:generate");
        return command;
    }

    private List<String> prepareGradleCommand() {
        List<String> command = new ArrayList<>();
        return command;
    }
    
}
