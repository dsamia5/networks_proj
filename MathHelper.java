import java.util.Scanner;
import java.util.Stack;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MathHelper {
    public Stack<Double> operandStack;
    public Stack<String> operatorStack;
    public Pattern tokenRegex;

    public MathHelper() {
        this.operandStack = new Stack<Double>();
        this.operatorStack = new Stack<String>();
    }

    // Parse and evaluate a mathematical expression
    public double evaluate(String expression) {
        Scanner sc = new Scanner(expression);
        String tokenRegex = "(\\d+\\.?\\d*)|[*/+-^\\(\\)]";
        String token;
        // true when there is no operand before an operator
        boolean unaryOperator = true;

        // Iterate through all tokens in the expression
        while((token = sc.findWithinHorizon(tokenRegex, 0)) != null) {
            if("+-*/^".contains(token)) {
                // Mark operator as unary if necessary
                if(unaryOperator) {
                    token = "u" + token;
                }

                int precedence = MathHelper.getOperatorPrecedence(token);
                // Process any operators of higher precedence on the opstack
                while(!this.operatorStack.empty() && MathHelper.getOperatorPrecedence(this.operatorStack.peek()) >= precedence) {
                    this.processOperator(this.operatorStack.pop());
                }
                // Push operator onto stack
                this.operatorStack.push(token);

                // If next token is an operator, it must be unary
                unaryOperator = true;
            // Push left paranthesis onto operator stack
            } else if("(".equals(token)) {
                this.operatorStack.push(token);
            // Pop from the operator stack until the corresponding opening paranthesis is gone
            } else if(")".equals(token)) {
                String operator;
                while(!this.operatorStack.empty() && !"(".equals(operator = this.operatorStack.pop())) {
                    try {
                        this.processOperator(operator);
                    } catch(Exception e) {
                        return Double.NaN;
                    }
                }
            } else {
                double operand = Double.parseDouble(token);
                this.operandStack.push(operand);

                // If next token is an operator, it must be binary
                unaryOperator = false;
            }
        }
        // Evaluate remaining operators on the stack
        while(!this.operatorStack.empty()) {
            try {
                this.processOperator(this.operatorStack.pop());
            } catch(Exception e) {
                return Double.NaN;
            }
        }

        // Pop result from stack and return
        if(!this.operandStack.empty()) {
            return this.operandStack.pop();
        } else {
            return Double.NaN;
        }
    }

    public void processOperator(String operator) {
        switch(operator) {
            case "+":
                this.add();
                break;
            case "-":
                this.subtract();
                break;
            case "u-":
                this.negate();
                break;
            case "*":
                this.multiply();
                break;
            case "/":
                this.divide();
                break;
            case "^":
                this.pow();
                break;
        }
    }
    
    public void add() {
        double a = this.operandStack.pop();
        double b = this.operandStack.pop();
        this.operandStack.push(b + a);
    }

    public void subtract() {
        double a = this.operandStack.pop();
        double b = this.operandStack.pop();
        this.operandStack.push(b - a);
    }

    public void negate() {
        double a = this.operandStack.pop();
        this.operandStack.push(-a);
    }

    public void multiply() {
        double a = this.operandStack.pop();
        double b = this.operandStack.pop();
        this.operandStack.push(b * a);
    }

    public void divide() {
        double a = this.operandStack.pop();
        double b = this.operandStack.pop();
        this.operandStack.push(b / a);
    }

    public void pow() {
        double a = this.operandStack.pop();
        double b = this.operandStack.pop();
        this.operandStack.push(Math.pow(b, a));
    }

    public static int getOperatorPrecedence(String operator) {
        switch(operator) {
            case "^":
                return 3;
            case "u+":
            case "u-":
                return 2;
            case "*":
            case "/":
                return 1;
            case "+":
            case "-":
                return 0;
            default:
                return -1;
        }
    }

    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        while(sc.hasNext()) {
            MathHelper mh = new MathHelper();
            System.out.println(mh.evaluate(sc.nextLine()));
        }
        sc.close();
    }
}
