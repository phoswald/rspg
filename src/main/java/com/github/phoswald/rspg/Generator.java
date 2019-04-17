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

import lombok.AllArgsConstructor;

public class Generator {

    private final Grammar grammar;
    private final Set<Token.Type> tokenTypes = new HashSet<>();
    private final Map<String, List<Argument>> callbacks = new LinkedHashMap<>();

    public Generator(Grammar grammar) {
        this.grammar = Objects.requireNonNull(grammar);
    }

    public void generate(Path targetPath) throws IOException {
        String javaPackage = grammar.getJavaType().substring(0, grammar.getJavaType().lastIndexOf("."));
        String javaClass = grammar.getJavaType().substring(grammar.getJavaType().lastIndexOf(".") + 1);
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
        for (Rule rule : grammar.getRules()) {
            generateRule(writer, rule);
        }
        generateHelpers(writer);
        generateCallbackInterface(writer);
        writer.write("}\n");
    }

    private void generateRule(Writer writer, Rule rule) throws IOException {
        if (rule.isExport()) {
            writer.write("\n");
            writer.write("    public " + rule.getJavaType() + " parse" + rule.getName() + "(String input) {\n");
            writer.write("        this.input = input;\n");
            writer.write("        this.offset = 0;\n");
            writer.write("        Ref<" + rule.getJavaType() + "> output = new Ref<>();\n");
            writer.write("        if (parse" + rule.getName() + "(output) && this.offset == input.length()) {\n");
            writer.write("            return output.value;\n");
            writer.write("        } else {\n");
            writer.write("            return null;\n");
            writer.write("        }\n");
            writer.write("    }\n");
        }
        writer.write("\n");
        writer.write("    private boolean parse" + rule.getName() + "(Ref<" + rule.getJavaType() + "> output) {\n");
        writer.write("        int offset = this.offset;\n");
        boolean emptyAlternative = false;
        boolean firstAlternative = true;
        for (Alternative alternative : rule.getAlternatives()) {
            if (!firstAlternative) {
                writer.write("        this.offset = offset;\n");
            }
            firstAlternative = false;
            writer.write("        {\n");
            String indent = "";
            int nr = 0;
            List<Argument> callbackArgs = new ArrayList<>();
            for (Element element : alternative.getElements()) {
                if (element instanceof Token) {
                    Token token = (Token) element;
                    if (token.isPass()) {
                        nr++;
                        writer.write("            " + indent + "int offset" + nr + " = this.offset;\n");
                    }
                    tokenTypes.add(token.getType());
                    writer.write("            " + indent + "if (match" + token.getType() + "(\"" + token.getText() + "\")) {\n");
                    if (token.isPass()) {
                        writer.write("                " + indent + "String token" + nr + " = input.substring(offset" + nr + ", this.offset);\n");
                        callbackArgs.add(new Argument("String", "token" + nr, "token" + nr));
                    }
                } else if (element instanceof Symbol) {
                    Symbol symbol = (Symbol) element;
                    if (!symbol.isLinked()) {
                        nr++;
                        String symbolJavaType = findRule(symbol).getJavaType();
                        writer.write("            " + indent + "Ref<" + symbolJavaType + "> element" + nr + " = new Ref<>();\n");
                        writer.write("            " + indent + "if (parse" + symbol.getName() + "(element" + nr + ")) {\n");
                        callbackArgs.add(new Argument(symbolJavaType, "element" + nr, "element" + nr + ".value"));
                    } else {
                        writer.write("            " + indent + "if (parse" + symbol.getName() + "(output)) {\n");
                    }
                } else {
                    throw new IllegalStateException("Unknown element type");
                }
                indent += "    ";
                if (element.isCallbackLinked()) {
                    callbackArgs.add(0, new Argument(rule.getJavaType(), "output", "output.value"));
                }
                if (element.getCallback() != null) {
                    String argumentExprs = getArgumentExprs(callbackArgs);
                    writer.write("            " + indent + "output.value = callback." + element.getCallback() + "(" + argumentExprs + ");\n");
                    callbacks.put(element.getCallback(), callbackArgs);
                    callbackArgs = new ArrayList<>();
                }
            }
            writer.write("            " + indent + "return true;\n");
            while (!indent.isEmpty()) {
                indent = indent.substring(4);
                writer.write("            " + indent + "}\n");
            }
            writer.write("        }\n");
            if (alternative.getElements().isEmpty()) {
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
        for (Rule rule : grammar.getRules()) {
            for (Alternative alternative : rule.getAlternatives()) {
                for (Element element : alternative.getElements()) {
                    if (element.getCallback() != null) {
                        String argumentDecls = getArgumentDecls(callbacks.get(element.getCallback()));
                        writer.write("\n");
                        writer.write("        public " + rule.getJavaType() + " " + element.getCallback() + "(" + argumentDecls + ");\n");
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
        return grammar.getRules().stream() //
                .filter(rule -> Objects.equals(rule.getName(), symbol.getName())) //
                .findFirst() //
                .orElseThrow(() -> new IllegalStateException("Rule not found: " + symbol.getName()));
    }

    @AllArgsConstructor
    private static class Argument {
        private final String type;
        private final String name;
        private final String expr;
    }
}
