package org.jopendocument.dom;

/**
 * Created with IntelliJ IDEA.
 * User: iskorodumov
 * Date: 14.05.15
 * Time: 11:00
 */
public class MyStyledNode {
    public static Style getStyle(StyledNode node, String name) {
        return node.getStyle(name);
    }
}
