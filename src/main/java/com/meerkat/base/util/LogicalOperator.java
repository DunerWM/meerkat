package com.meerkat.base.util;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by wm on 16/12/14.
 */
public abstract class LogicalOperator {

    /**
     * 运算符
     */
    private String operator;

    /**
     * 运算符的优先级别，数字越大，优先级别越高
     */
    private int priority;

    private static Map<String, LogicalOperator> operators = new HashMap<String, LogicalOperator>();

    private LogicalOperator(String operator, int priority) {
        setOperator(operator);
        setPriority(priority);
        register(this);
    }

    private void register(LogicalOperator operator) {
        operators.put(operator.getOperator(), operator);
    }

    /**
     * 大于运算
     */
    public final static LogicalOperator GREAT_THAN = new LogicalOperator(">", 100) {
        public Boolean eval(Object left, Object right) {
            if (left instanceof Integer && right instanceof Integer) {
                return (int) left > (int) right;
            } else if (left instanceof Double && right instanceof Double) {
                return (double) left > (double) right;
            } else if (left instanceof BigDecimal && right instanceof BigDecimal) {
                return ((BigDecimal) left).compareTo((BigDecimal) right) > 0;
            } else if (left instanceof Float && right instanceof Float) {
                return ((Float) left).compareTo((Float) right) > 0;
            } else {
                throw new RuntimeException("参数类型不一致，不可比较");
            }
        }
    };

    /**
     * 小于运算
     */
    public final static LogicalOperator LESS_THAN = new LogicalOperator("<", 100) {
        public Boolean eval(Object left, Object right) {
            if (left instanceof Integer && right instanceof Integer) {
                return (int) left < (int) right;
            } else if (left instanceof Double && right instanceof Double) {
                return (double) left < (double) right;
            } else if (left instanceof BigDecimal && right instanceof BigDecimal) {
                return ((BigDecimal) left).compareTo((BigDecimal) right) < 0;
            } else if (left instanceof Float && right instanceof Float) {
                return ((Float) left).compareTo((Float) right) < 0;
            } else {
                throw new RuntimeException("参数类型不一致，不可比较");
            }
        }
    };

    /**
     * 大于等于运算
     */
    public final static LogicalOperator GREAT_THAN_EQUAL = new LogicalOperator(">=", 100) {
        public Boolean eval(Object left, Object right) {
            if (left instanceof Integer && right instanceof Integer) {
                return (int) left >= (int) right;
            } else if (left instanceof Double && right instanceof Double) {
                return (double) left >= (double) right;
            } else if (left instanceof BigDecimal && right instanceof BigDecimal) {
                return ((BigDecimal) left).compareTo((BigDecimal) right) >= 0;
            } else if (left instanceof Float && right instanceof Float) {
                return ((Float) left).compareTo((Float) right) >= 0;
            } else {
                throw new RuntimeException("参数类型不一致，不可比较");
            }
        }
    };

    /**
     * 小于等于运算
     */
    public final static LogicalOperator LESS_THAN_EQUAL = new LogicalOperator("<=", 100) {
        public Boolean eval(Object left, Object right) {
            if (left instanceof Integer && right instanceof Integer) {
                return (int) left <= (int) right;
            } else if (left instanceof Double && right instanceof Double) {
                return (double) left <= (double) right;
            } else if (left instanceof BigDecimal && right instanceof BigDecimal) {
                return ((BigDecimal) left).compareTo((BigDecimal) right) <= 0;
            } else if (left instanceof Float && right instanceof Float) {
                return ((Float) left).compareTo((Float) right) <= 0;
            } else {
                throw new RuntimeException("参数类型不一致，不可比较");
            }
        }
    };

    /**
     * 相等判断
     */
    public final static LogicalOperator EQUAL = new LogicalOperator("=", 100) {
        @Override
        public Boolean eval(Object left, Object right) {
            if (left instanceof Integer && right instanceof Integer) {
                return (int) left == (int) right;
            } else if (left instanceof Double && right instanceof Double) {
                return (double) left == (double) right;
            } else if (left instanceof BigDecimal && right instanceof BigDecimal) {
                return ((BigDecimal) left).compareTo((BigDecimal) right) == 0;
            } else if (left instanceof Float && right instanceof Float) {
                return ((Float) left).compareTo((Float) right) == 0;
            } else if (left instanceof String && right instanceof String) {
                return ((String) left).equals((String) right);
            } else {
                throw new RuntimeException("参数类型不一致，不可比较");
            }
        }
    };

    /**
     * 根据某个运算符获得该运算符的优先级别
     *
     * @param c
     * @return 运算符的优先级别
     */
    public static int getPrority(String c) {
        LogicalOperator op = operators.get(c);
        if (op != null) {
            return op.getPriority();
        }
        return 0;
    }

    /**
     * 工具方法，判断某个字符是否是运算符
     *
     * @param c
     * @return 是运算符返回 true，否则返回 false
     */
    public static boolean isOperator(String c) {
        return getInstance(c) != null;
    }

    /**
     * 根据运算符获得 Operator 实例
     *
     * @param c
     * @return 从注册中的 Operator 返回实例，尚未注册返回 null
     */
    public static LogicalOperator getInstance(String c) {
        return operators.get(c);
    }

    /**
     * 根据操作数进行计算
     *
     * @param left  左操作数
     * @param right 右操作数
     * @return 计算结果
     */
    public abstract Boolean eval(Object left, Object right);


    public String getOperator() {
        return operator;
    }

    public void setOperator(String operator) {
        this.operator = operator;
    }

    public int getPriority() {
        return priority;
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }
}
