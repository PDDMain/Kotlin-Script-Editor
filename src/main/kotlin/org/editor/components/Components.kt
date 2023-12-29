package org.editor.components

import org.editor.styles.BACKGROUND_LIGHT_DARK
import org.editor.styles.FOREGROUND_LIGHT
import org.editor.styles.OUTPUT_FONT_BLACK
import java.awt.*
import java.awt.geom.Path2D
import javax.swing.*
import javax.swing.border.LineBorder
import javax.swing.text.*


class TriangleIcon(private val color: Color, private val width: Int, private val height: Int) : Icon {
    override fun paintIcon(c: Component, g: Graphics, x: Int, y: Int) {
        val g2d = g.create() as Graphics2D

        val path: Path2D = Path2D.Double()
        path.moveTo(x.toDouble(), y.toDouble())
        path.lineTo(x.toDouble(), (y + height).toDouble())
        path.lineTo((x + width).toDouble(), (y + (height / 2)).toDouble())
        path.closePath()

        g2d.color = color
        g2d.fill(path)

        g2d.dispose()
    }

    override fun getIconWidth(): Int {
        return width
    }

    override fun getIconHeight(): Int {
        return height
    }
}

class JTextPaneWithHeader(headerText: String) : JPanel() {
    val textPane: JTextPane

    init {
        layout = BorderLayout()

        border = LineBorder(OUTPUT_FONT_BLACK)
        background = BACKGROUND_LIGHT_DARK
        // Create header label
        val headerLabel = JLabel(headerText)
        headerLabel.foreground = FOREGROUND_LIGHT
        headerLabel.horizontalAlignment = SwingConstants.CENTER
        headerLabel.border = LineBorder(OUTPUT_FONT_BLACK)
        add(headerLabel, BorderLayout.NORTH)

        // Create text pane
        textPane = JTextPane()
        val scrollPane = JScrollPane(textPane)
        scrollPane.border = LineBorder(OUTPUT_FONT_BLACK)
        add(textPane, BorderLayout.CENTER)
    }
}

fun appendToPane(textPane: JTextPane, text: String, color: Color = Color.DARK_GRAY) {
    val styleContext = StyleContext.getDefaultStyleContext()
    val attributeSet = styleContext.addAttribute(SimpleAttributeSet.EMPTY, StyleConstants.Foreground, color)

    val length = textPane.document.length
    textPane.document.insertString(length, text, attributeSet)
}

fun setTabSize(textPane: JTextPane, spaces: Int = 4) {
    val doc: DefaultStyledDocument = textPane.styledDocument as DefaultStyledDocument
    val tabs = arrayOfNulls<TabStop>(20)
    for (i in tabs.indices) {
        tabs[i] = TabStop(
            (spaces * textPane.getFontMetrics(textPane.font).charWidth(' ')).toFloat(),
            TabStop.ALIGN_LEFT,
            TabStop.LEAD_NONE
        )
    }
    val tabSet = TabSet(tabs)
    val attributes = SimpleAttributeSet()
    StyleConstants.setTabSet(attributes, tabSet)
    val length = doc.length
    doc.setParagraphAttributes(0, length, attributes, false)
}

