package com.github.phoswald.rspg;

import static com.github.phoswald.rspg.Alternative.alternative;
import static com.github.phoswald.rspg.Symbol.symbol;
import static com.github.phoswald.rspg.Token.set;
import static com.github.phoswald.rspg.Token.token;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Test;

class GeneratorTest {

    @Test
    void generate_calculator() throws IOException {
        // Arrange
        List<Rule> rules = new ArrayList<>();
        rules.add(Rule.builder() //
                .name("Expression") //
                .javaType("Double") //
                .export(true) //
                .alternatives(Arrays.asList( //
                        alternative(symbol("ExprAdd").withLinked()))) //
                .build());
        rules.add(Rule.builder() //
                .name("ExprAdd") //
                .javaType("Double") //
                .alternatives(Arrays.asList( //
                        alternative(symbol("ExprMul").withLinked(), symbol("OpAdd").withLinked()))) //
                .build());
        rules.add(Rule.builder() //
                .name("OpAdd") //
                .javaType("Double") //
                .alternatives(Arrays.asList( //
                        alternative(token("+"), symbol("ExprMul").withCallbackLinked("add"), symbol("OpAdd").withLinked()), //
                        alternative(token("-"), symbol("ExprMul").withCallbackLinked("sub"), symbol("OpAdd").withLinked()), //
                        alternative())) //
                .build());
        rules.add(Rule.builder() //
                .name("ExprMul") //
                .javaType("Double") //
                .alternatives(Arrays.asList( //
                        alternative(symbol("Brace").withLinked(), symbol("OpMul").withLinked()))) //
                .build());
        rules.add(Rule.builder() //
                .name("OpMul") //
                .javaType("Double") //
                .alternatives(Arrays.asList( //
                        alternative(token("*"), symbol("Brace").withCallbackLinked("mul"), symbol("OpMul").withLinked()), //
                        alternative(token("/"), symbol("Brace").withCallbackLinked("div"), symbol("OpMul").withLinked()), //
                        alternative())) //
                .build());
        rules.add(Rule.builder() //
                .name("Brace") //
                .javaType("Double") //
                .alternatives(Arrays.asList( //
                        alternative(token("("), symbol("Expression").withLinked(), token(")")), //
                        alternative(symbol("Value").withLinked()))) //
                .build());
        rules.add(Rule.builder() //
                .name("Value") //
                .javaType("Double") //
                .alternatives(Arrays.asList( //
                        alternative(token("pi").withCallback("getPi")), //
                        alternative(token("e").withCallback("getE")), //
                        alternative(symbol("Number").withCallback("createNumber")))) //
                .build());
        rules.add(Rule.builder() //
                .name("Number") //
                .javaType("Integer") //
                .alternatives(Arrays.asList( //
                        alternative(symbol("Digit").withLinked(), symbol("Digits").withLinked()))) //
                .build());
        rules.add(Rule.builder() //
                .name("Digit") //
                .javaType("Integer") //
                .alternatives(Arrays.asList( //
                        alternative(set("0123456789").withPass().withCallbackLinked("handleDigit")))) //
                .build());
        rules.add(Rule.builder() //
                .name("Digits") //
                .javaType("Integer") //
                .alternatives(Arrays.asList( //
                        alternative(symbol("Digit").withLinked(), symbol("Digits").withLinked()), //
                        alternative())) //
                .build());
        Grammar grammar = Grammar.builder() //
                .name("Calculator") //
                .javaType("parsers.Calculator") //
                .rules(rules) //
                .build();

        // Act
        new Generator(grammar).generate(Paths.get("target/generated-parsers"));

        // Assert
        assertEqualTextFiles( //
                Paths.get("src/test/java/parsers/Calculator.java"), //
                Paths.get("target/generated-parsers/parsers/Calculator.java"));
    }

    private void assertEqualTextFiles(Path expectedFile, Path actualFile) throws IOException {
        String expectedText = String.join("\n", Files.readAllLines(expectedFile));
        String actualText = String.join("\n", Files.readAllLines(actualFile));
        assertTrue(expectedText.equals(actualText), () -> "Files differ: " + expectedFile + ", " + actualFile);
    }
}
