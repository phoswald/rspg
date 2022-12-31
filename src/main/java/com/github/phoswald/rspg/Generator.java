package com.github.phoswald.rspg;

import java.io.IOException;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

public class Generator {

    private final Grammar grammar;
    private final Set<Token.Type> tokenTypes = new HashSet<>();
    private final Map<String, List<Argument>> callbacks = new LinkedHashMap<>();

    public Generator(Grammar grammar) {
        this.grammar = Objects.requireNonNull(grammar);
    }

    public void generate(Path targetPath) throws IOException {
        String javaPackage = grammar.javaType().substring(0, grammar.javaType().lastIndexOf("."));
        String javaClass = grammar.javaType().substring(grammar.javaType().lastIndexOf(".") + 1);
        Path targetPackage = targetPath.resolve(javaPackage.replace(".", "/"));
        Path targetClass = targetPackage.resolve(javaClass + ".java");
        Files.createDirectories(targetPackage);
        try (Writer writer = Files.newBufferedWriter(targetClass)) {
            generateParser(writer, javaPackage, javaClass);
        }
    }

    private void generateParser(Writer writer, String javaPackage, String javaClass) throws IOException {
        writer.write("package " + javaPackage + ";\n");
        writer.write("\n");
        writer.write("public class " + javaClass + " {\n");
        writer.write("\n");
        writer.write("    private final Callback callback;\n");
        writer.write("    private String input;\n");
        writer.write("    private int offset;\n");
        writer.write("\n");
        writer.write("    public " + javaClass + "(Callback callback) {\n");
        writer.write("        this.callback = callback;\n");
        writer.write("    }\n");
        for (Rule rule : grammar.rules()) {
            generateRule(writer, rule);
        }
        generateHelpers(writer);
        generateCallbackInterface(writer);
        writer.write("}\n");
    }

    private void generateRule(Writer writer, Rule rule) throws IOException {
        if (rule.export()) {
            writer.write("\n");
            writer.write("    public " + rule.javaType() + " parse" + rule.name() + "(String input) {\n");
            writer.write("        this.input = input;\n");
            writer.write("        this.offset = 0;\n");
            writer.write("        Ref<" + rule.javaType() + "> output = new Ref<>();\n");
            writer.write("        if (parse" + rule.name() + "(output) && this.offset == input.length()) {\n");
            writer.write("            return output.value;\n");
            writer.write("        } else {\n");
            writer.write("            return null;\n");
            writer.write("        }\n");
            writer.write("    }\n");
        }
        writer.write("\n");
        writer.write("    private boolean parse" + rule.name() + "(Ref<" + rule.javaType() + "> output) {\n");
        writer.write("        int offset = this.offset;\n");
        boolean emptyAlternative = false;
        boolean firstAlternative = true;
        for (Alternative alternative : rule.alternatives()) {
            if (!firstAlternative) {
                writer.write("        this.offset = offset;\n");
            }
            firstAlternative = false;
            writer.write("        {\n");
            String indent = "";
            int nr = 0;
            List<Argument> callbackArgs = new ArrayList<>();
            for (Element element : alternative.elements()) {
                if (element instanceof Token token) {
                    if (token.pass()) {
                        nr++;
                        writer.write("            " + indent + "int offset" + nr + " = this.offset;\n");
                    }
                    tokenTypes.add(token.type());
                    writer.write("            " + indent + "if (match" + token.type() + "(\"" + token.text() + "\")) {\n");
                    if (token.pass()) {
                        writer.write("                " + indent + "String token" + nr + " = input.substring(offset" + nr + ", this.offset);\n");
                        callbackArgs.add(new Argument("String", "token" + nr, "token" + nr));
                    }
                } else if (element instanceof Symbol symbol) {
                    if (!symbol.linked()) {
                        nr++;
                        String symbolJavaType = findRule(symbol).javaType();
                        writer.write("            " + indent + "Ref<" + symbolJavaType + "> element" + nr + " = new Ref<>();\n");
                        writer.write("            " + indent + "if (parse" + symbol.name() + "(element" + nr + ")) {\n");
                        callbackArgs.add(new Argument(symbolJavaType, "element" + nr, "element" + nr + ".value"));
                    } else {
                        writer.write("            " + indent + "if (parse" + symbol.name() + "(output)) {\n");
                    }
                } else {
                    throw new IllegalStateException("Unknown element type");
                }
                indent += "    ";
                if (element.callbackLinked()) {
                    callbackArgs.add(0, new Argument(rule.javaType(), "output", "output.value"));
                }
                if (element.callback() != null) {
                    String argumentExprs = getArgumentExprs(callbackArgs);
                    writer.write("            " + indent + "output.value = callback." + element.callback() + "(" + argumentExprs + ");\n");
                    callbacks.put(element.callback(), callbackArgs);
                    callbackArgs = new ArrayList<>();
                }
            }
            writer.write("            " + indent + "return true;\n");
            while (!indent.isEmpty()) {
                indent = indent.substring(4);
                writer.write("            " + indent + "}\n");
            }
            writer.write("        }\n");
            if (alternative.elements().isEmpty()) {
                emptyAlternative = true;
            }
        }
        if (!emptyAlternative) {
            writer.write("        this.offset = offset;\n");
            writer.write("        return false;\n");
        }
        writer.write("    }\n");
    }

    private void generateHelpers(Writer writer) throws IOException {
        if (tokenTypes.contains(Token.Type.Token)) {
            writer.write("\n");
            writer.write("    private boolean matchToken(String token) {\n");
            writer.write("        if (this.input.startsWith(token, this.offset)) {\n");
            writer.write("            this.offset += token.length();\n");
            writer.write("            return true;\n");
            writer.write("        } else {\n");
            writer.write("            return false;\n");
            writer.write("        }\n");
            writer.write("    }\n");
        }
        if (tokenTypes.contains(Token.Type.Set)) {
            writer.write("\n");
            writer.write("    private boolean matchSet(String set) {\n");
            writer.write("        if (offset < input.length() && set.contains(input.substring(offset, offset + 1))) {\n");
            writer.write("            this.offset++;\n");
            writer.write("            return true;\n");
            writer.write("        } else {\n");
            writer.write("            return false;\n");
            writer.write("        }\n");
            writer.write("    }\n");
        }
        writer.write("\n");
        writer.write("    private static class Ref<T> {\n");
        writer.write("        T value;\n");
        writer.write("    }\n");
    }

    private void generateCallbackInterface(Writer writer) throws IOException {
        writer.write("\n");
        writer.write("    public static interface Callback {\n");
        for (Rule rule : grammar.rules()) {
            for (Alternative alternative : rule.alternatives()) {
                for (Element element : alternative.elements()) {
                    if (element.callback() != null) {
                        String argumentDecls = getArgumentDecls(callbacks.get(element.callback()));
                        writer.write("\n");
                        writer.write("        public " + rule.javaType() + " " + element.callback() + "(" + argumentDecls + ");\n");
                    }
                }
            }
        }
        writer.write("    }\n");
    }

    private String getArgumentExprs(List<Argument> arguments) {
        return arguments.stream() //
                .map(ca -> ca.expr) //
                .collect(Collectors.joining(", "));
    }

    private String getArgumentDecls(List<Argument> arguments) {
        return arguments.stream() //
                .map(ca -> ca.type + " " + ca.name) //
                .collect(Collectors.joining(", "));
    }

    private Rule findRule(Symbol symbol) {
        return grammar.rules().stream() //
                .filter(rule -> Objects.equals(rule.name(), symbol.name())) //
                .findFirst() //
                .orElseThrow(() -> new IllegalStateException("Rule not found: " + symbol.name()));
    }

    private record Argument(String type, String name, String expr) { }
}
