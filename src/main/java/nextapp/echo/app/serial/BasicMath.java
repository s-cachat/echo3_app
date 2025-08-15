package nextapp.echo.app.serial;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * La formue peut contenir des parethèses, les opérateurs *,-,/,+ et des nombre
 * avec un point décimal
 *
 * <expression> ::= <term> { ("+"|"-") <term> }
 *
 * <term> ::= <factor> { ("*"|"/") <factor> }
 *
 * <factor> ::= "(" <expression> ")" | "-" <factor> | <number>
 * <number> ::= { <digit> }+ [ "." { <digit> }* ]
 * <digit> ::= "0" | "1" | "2" | "3" | "4" | "5" | "6" | "7" | "8" | "9"
 */
public class BasicMath {

    /**
     * nombre maximum de chiffres pour les nombres
     */
    private final static int MaxNumOfDigit = 38; // max NUMBER of digits for numbers
    /**
     * pattern pour détecter une variable
     */
    public static final Pattern variablePattern = Pattern.compile("[a-zA-Z_][a-zA-Z_0-9]*");

    /**
     * effectue le calcul demandé, en remplacant les variables par leur valeur
     *
     * @param formula la formule a évaluer
     * @param variables la table des variables (une variable a un nom composé de
     * lettres majuscules ou minuscules, de chiffres ou d'underscore. Le nom ne
     * peut commencer par un chiffre)
     * @return la valeur (ou Double.NaN si une des variables utilisée est NaN)
     * @throws Exception en cas d'erreur
     */
    public static double evaluate(String formula, Map<String, Double> variables) throws Exception {
        StringBuilder sb = new StringBuilder();
        Matcher mat = variablePattern.matcher(formula);
        while (mat.find()) {
            Double value = variables.get(mat.group(0));
            if (value == null) {
                mat.appendReplacement(sb, mat.group(0));
            } else if (value.isNaN()) {
                return Double.NaN;
            } else {
                mat.appendReplacement(sb, String.valueOf(value));
            }
        }
        mat.appendTail(sb);
        Node n = parseExpression(sb.toString());
        return evaluate(n);
    }

    /**
     * effectue le calcul demandé
     *
     * @param formula la formule a évaluer
     * @return la valeur
     * @throws Exception en cas d'erreur
     */
    public static double evaluate(String formula) throws Exception {
        Node n = parseExpression(formula);
        return evaluate(n);
    }

    /**
     * effectue les calculs pour le noeud donné
     *
     * @param p le noeud
     * @return la valeur
     */
    private static double evaluate(Node p) {
        if (p != null) {
            return switch (p.type) {
                case ADD ->
                    evaluate(p.left) + evaluate(p.right);
                case SUBSTRACT ->
                    evaluate(p.left) - evaluate(p.right);
                case MULTIPLY ->
                    evaluate(p.left) * evaluate(p.right);
                case DIVIDE ->
                    evaluate(p.left) / evaluate(p.right);
                case MINUS ->
                    -evaluate(p.left);
                case NUMBER ->
                    p.num;
                default ->
                    throw new RuntimeException("unexpected type: " + p.type);
            };
        }
        return 0;
    }

    private static Node parseExpression(String expr) throws Exception {
        StringHolder sh = new StringHolder(expr);
        Node result = expression(sh);
        if (sh.chars_available()) {
            throw new Exception("unexpected character: '" + expr.charAt(sh.pointer) + "' at position " + sh.pointer);
        }
        return result;
    }

    private static Node expression(StringHolder sh) throws Exception {
        Node pt = term(sh);
        Character ch;
        while ((ch = sh.nextChar("+-")) != null) {
            Node p = new Node(ch == '+' ? Type.ADD : Type.SUBSTRACT);
            p.left = pt;
            pt = p;
            p.right = term(sh);
        }
        return pt;
    }

    private static Node term(StringHolder sh) throws Exception {
        Node pf = factor(sh);
        Character ch;
        while ((ch = sh.nextChar("*/")) != null) {
            Node p = new Node(ch == '*' ? Type.MULTIPLY : Type.DIVIDE);
            p.left = pf;
            pf = p;
            p.right = factor(sh);
        }
        return pf;
    }

    private static Node factor(StringHolder sh) throws Exception {
        Character ch = sh.nextChar("(-0123456789");
        if (ch == null) {
            if (sh.chars_available()) {
                throw new Exception("unexpected character: '" + sh.expr.charAt(sh.pointer) + "' at position " + sh.pointer);
            } else {
                throw new Exception("unexpected end of expression");
            }
        }
        if (ch == '(') {
            Node n1 = expression(sh);
            ch = sh.nextChar(")");
            if (ch == null) {
                throw new Exception("missing ) bracket");
            }
            return n1;
        } else if (ch == '-') {
            Node n2 = new Node(Type.MINUS);
            n2.left = factor(sh);
            return n2;
        } else if (ch >= '0' && ch <= '9') {
            double num = 0;
            int nDigits = 0;
            while (ch != null) {
                if (nDigits < MaxNumOfDigit) {
                    num = num * 10 + ch - '0';
                    nDigits++;
                } else {
                    num = num * 10;
                }
                ch = sh.nextChar("0123456789");
            }
            if (sh.nextChar(".") != null) {
                double m = 1;
                while ((ch = sh.nextChar("0123456789")) != null) {
                    m = m * 0.1;
                    num = num + (ch - '0') * m;
                }
            }
            return new Node(num);
        } else {
            return null;
        }
    }

    enum Type {
        NUMBER, MINUS, ADD, SUBSTRACT, MULTIPLY, DIVIDE
    }

    private static class Node {

        private final Type type;
        private Node left;
        private Node right;
        private double num;

        Node(Type type) {
            this.type = type;
            left = null;
            right = null;
        }

        Node(double num) {
            type = Type.NUMBER;
            left = null;
            right = null;
            this.num = num;
        }
    }

    private static class StringHolder {

        private final String expr;
        private int pointer;

        StringHolder(String expr) {
            this.expr = expr;
            pointer = 0;
        }

        private Character nextChar(String charset) {
            Character ch = null;
            while ((pointer < expr.length()) && (ch = expr.charAt(pointer++)) == ' ') {
            }
            if (ch != null) {
                if (charset.indexOf(ch) != -1) {
                    return ch;
                }
                pointer--;
            }
            return null;
        }

        private boolean chars_available() {
            return pointer < expr.length();
        }

    }
}
