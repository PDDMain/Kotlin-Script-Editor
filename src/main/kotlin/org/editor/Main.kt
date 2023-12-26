//package org.editor
//
//import org.executor.ScriptExecutor
//import java.awt.*
//import java.awt.geom.Path2D
//import java.io.BufferedReader
//import java.io.File
//import java.io.IOException
//import java.io.InputStreamReader
//import java.nio.file.Files
//import java.nio.file.Paths
//import javax.swing.*
//import javax.swing.border.EmptyBorder
//import kotlin.io.path.ExperimentalPathApi
//import kotlin.io.path.deleteRecursively
//
//class App {
//    private val textEditorFrame: JFrame
//    private val codeTextArea: JTextAreaWithHeader
//    private val outputTextPanel: JTextAreaWithHeader
//    private val scriptExecutor = ScriptExecutor(File(".pdd.script/script.kts"))
//
//    init {
//        textEditorFrame = createTextEditorFrame()
//
//        // Create a split pane with a 70%-30% split
//
//
//        codeTextArea = createCodeTextArea()
//        outputTextPanel = createOutputTextArea()
//
//        val splitPane = JSplitPane(JSplitPane.VERTICAL_SPLIT, codeTextArea, outputTextPanel)
//        splitPane.resizeWeight = 0.7 // 70% to the top component, 30% to the bottom component
//
//        textEditorFrame.add(splitPane)
//
//        textEditorFrame.jMenuBar = createMenuBar()
//
//        textEditorFrame.isVisible = true
//    }
//
//    fun createTextEditorFrame(): JFrame {
//        val textEditorFrame = JFrame("Text Editor")
//        textEditorFrame.defaultCloseOperation = JFrame.EXIT_ON_CLOSE
//        textEditorFrame.setSize(1080, 800)
//        textEditorFrame.setLocationRelativeTo(null);
//        return textEditorFrame
//    }
//
//    fun createMenuBar(): JMenuBar {
//        val menuBar = JMenuBar()
//
//        val menuFile = JMenu("File")
//
//        val menuItemNEW = JMenuItem("New")
//        val menuItemOPEN = JMenuItem("Open")
//        val menuItemSAVE = JMenuItem("Save")
//        val menuItemQUIT = JMenuItem("Quit")
//
//        menuBar.add(menuFile)
//
//        menuFile.add(menuItemNEW)
//        menuFile.add(menuItemOPEN)
//        menuFile.add(menuItemSAVE)
//        menuFile.add(menuItemQUIT)
//
//        val triangleButton: JButton = createTriangleButton()
//        menuBar.add(Box.createHorizontalGlue()) // Push the button to the right
//        menuBar.add(triangleButton)
//        return menuBar
//    }
//
//    fun createCodeTextArea(): JTextAreaWithHeader {
//        val code = JTextAreaWithHeader("Kotlin Script:");
//        code.textArea.foreground = Color.DARK_GRAY
//
//        val padding = 10
//        code.setBorder(EmptyBorder(padding, padding, padding, padding))
//
//        code.textArea.tabSize = 4
//
//        setFont(code.textArea)
//
//        return code
//    }
//
//    fun createOutputTextArea(): JTextAreaWithHeader {
//        val outputTextArea = JTextAreaWithHeader("Output:");
//        outputTextArea.textArea.isEditable = false
//        outputTextArea.textArea.foreground = Color.DARK_GRAY
//
//        val padding = 10
//        outputTextArea.setBorder(EmptyBorder(padding, padding, padding, padding))
//
//        outputTextArea.textArea.tabSize = 4
//
//        setFont(outputTextArea.textArea)
//
//        return outputTextArea
//    }
//
//    private fun setFont(outputTextArea: JTextArea) {
//        try {
//            var customFont = Font.createFont(Font.TRUETYPE_FONT, File("fonts/JetBrainsMono-Regular.ttf"))
//            customFont = customFont.deriveFont(Font.PLAIN, 14f) // Set font size (adjust as needed)
//            outputTextArea.setFont(customFont)
//        } catch (e: FontFormatException) {
//            e.printStackTrace()
//            // Handle exception if font loading fails
//        } catch (e: IOException) {
//            e.printStackTrace()
//        }
//    }
//
//    private fun createTriangleButton(): JButton {
//        val button = JButton()
//        button.icon = TriangleIcon(Color.decode("#008000"), 10, 10)
//        button.isBorderPainted = false
//        button.isFocusPainted = false
//        button.isContentAreaFilled = false
//        button.addActionListener {
//
//            // Disable the button to prevent multiple clicks
//            button.isEnabled = false
//            outputTextPanel.textArea.text = ""
//
//            val worker = object : SwingWorker<Unit, String>() {
//                override fun doInBackground() {
//                    val process = scriptExecutor.runKotlinScript(codeTextArea.textArea.text)
//                    val reader = BufferedReader(InputStreamReader(process.inputStream))
//
//                    while (true) {
//                        val line = reader.readLine() ?: break
//                        publish(line)
//                    }
//                    publish("Exit code: ${process.waitFor()}")
//                }
//
//                override fun process(chunks: MutableList<String>) {
//                    for (chunk in chunks)
//                    outputTextPanel.textArea.append(chunk + "\n")
//                }
//
//                override fun done() {
//                    button.isEnabled = true
//                }
//            }
//
//            worker.execute()
//
//        }
//        return button
//    }
//
//    private class TriangleIcon(private val color: Color, private val width: Int, private val height: Int) : Icon {
//        override fun paintIcon(c: Component, g: Graphics, x: Int, y: Int) {
//            val g2d = g.create() as Graphics2D
//
//            val path: Path2D = Path2D.Double()
//            path.moveTo(x.toDouble(), y.toDouble())
//            path.lineTo(x.toDouble(), (y + height).toDouble())
//            path.lineTo((x + width).toDouble(), (y + (height / 2)).toDouble())
//            path.closePath()
//
//            g2d.color = color
//            g2d.fill(path)
//
//            g2d.dispose()
//        }
//
//        override fun getIconWidth(): Int {
//            return width
//        }
//
//        override fun getIconHeight(): Int {
//            return height
//        }
//    }
//
//    class JTextAreaWithHeader(headerText: String) : JPanel() {
//        val textArea: JTextArea
//
//        init {
//            layout = BorderLayout()
//
//            // Create header label
//            val headerLabel = JLabel(headerText)
//            headerLabel.horizontalAlignment = SwingConstants.CENTER
//            add(headerLabel, BorderLayout.NORTH)
//
//            // Create text area
//            textArea = JTextArea()
//            val scrollPane = JScrollPane(textArea)
//            add(scrollPane, BorderLayout.CENTER)
//        }
//    }
//}
//
//@OptIn(ExperimentalPathApi::class)
//fun main() {
//    if (Files.exists(Paths.get("./.pdd.script"))) {
//        Paths.get("./.pdd.script").deleteRecursively()
//    }
//    Files.createDirectory(Paths.get("./.pdd.script"))
//
//    App()
//}

package org.editor

import org.executor.ScriptExecutor
import java.awt.*
import java.awt.event.ActionEvent
import java.awt.event.ActionListener
import java.awt.geom.Path2D
import java.io.BufferedReader
import java.io.File
import java.io.IOException
import java.io.InputStreamReader
import java.nio.file.Files
import java.nio.file.Paths
import javax.swing.*
import javax.swing.border.EmptyBorder
import javax.swing.event.DocumentEvent
import javax.swing.event.DocumentListener
import javax.swing.text.*
import kotlin.io.path.ExperimentalPathApi
import kotlin.io.path.deleteRecursively


class App {
    private val textEditorFrame: JFrame
    private val codeTextPane: JTextPaneWithHeader
    private val outputTextPane: JTextPaneWithHeader
    private var progressBar: JProgressBar = JProgressBar(0, 10)
    private val totalTimeInSeconds = 10 // Set the total time in seconds
    private val timer: Timer = Timer(1000, object : ActionListener {
        var currentTime: Int = totalTimeInSeconds

        override fun actionPerformed(e: ActionEvent) {
            progressBar.setValue(currentTime)
            progressBar.setString("Time Left: $currentTime seconds")

            if (currentTime != 0) {
                currentTime--
            }
        }
    })

    private val scriptExecutor = ScriptExecutor(File(".pdd.script/script.kts"))

    init {
        textEditorFrame = createTextEditorFrame()

        // Create a split pane with a 70%-30% split
        codeTextPane = createCodeTextPane()
        outputTextPane = createOutputTextPane()

        val splitPane = JSplitPane(JSplitPane.VERTICAL_SPLIT, codeTextPane, outputTextPane)
        splitPane.resizeWeight = 0.7 // 70% to the top component, 30% to the bottom component

        textEditorFrame.add(splitPane)


        textEditorFrame.jMenuBar = createMenuBar()

        textEditorFrame.isVisible = true
    }

    fun createTextEditorFrame(): JFrame {
        val textEditorFrame = JFrame("Text Editor")
        textEditorFrame.defaultCloseOperation = JFrame.EXIT_ON_CLOSE
        textEditorFrame.setSize(1080, 800)
        textEditorFrame.setLocationRelativeTo(null);
        return textEditorFrame
    }

    fun createMenuBar(): JMenuBar {
        val menuBar = JMenuBar()

        val menuFile = JMenu("File")

        val menuItemNEW = JMenuItem("New")
        val menuItemOPEN = JMenuItem("Open")
        val menuItemSAVE = JMenuItem("Save")
        val menuItemQUIT = JMenuItem("Quit")

        menuBar.add(menuFile)

        menuFile.add(menuItemNEW)
        menuFile.add(menuItemOPEN)
        menuFile.add(menuItemSAVE)
        menuFile.add(menuItemQUIT)

        val triangleButton: JButton = createTriangleButton()
        menuBar.add(Box.createHorizontalGlue()) // Push the button to the right

        createProgressBar()
        menuBar.add(progressBar)
        menuBar.add(triangleButton)
        return menuBar
    }

    private fun createProgressBar() {
        progressBar.setStringPainted(true)
        progressBar.isStringPainted = true
        progressBar.foreground = Color(50, 150, 50) // Custom foreground color
        progressBar.font = Font("Arial", Font.BOLD, 14) // Custom font

    }

    fun createCodeTextPane(): JTextPaneWithHeader {
        val code = JTextPaneWithHeader("Kotlin Script:")
        code.textPane.foreground = Color.DARK_GRAY

        val padding = 10
        code.setBorder(EmptyBorder(padding, padding, padding, padding))

        setFont(code.textPane)

        val highlightTimer = Timer(100) {
            highlightOccurrences(code.textPane)
        }

        setTabSize(code.textPane, 4)

        code.textPane.document.addDocumentListener(object : DocumentListener {
            override fun insertUpdate(e: DocumentEvent) {
                highlightTimer.restart()
            }

            override fun removeUpdate(e: DocumentEvent) {
                highlightTimer.restart()
            }

            override fun changedUpdate(e: DocumentEvent) {
                // Plain text components don't fire these events
            }
        })
        return code
    }

    private fun setTabSize(textPane: JTextPane, spaces: Int = 4) {
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

    val KEYWORDS = setOf(
        Pair("val", Color.BLUE),
        Pair("var", Color.BLUE),
        Pair("fun", Color.BLUE),
        Pair("while", Color.BLUE),
        Pair("if", Color.BLUE),
        Pair("for", Color.BLUE),
        Pair("true", Color.BLUE),
        Pair("false", Color.BLUE),
    )

    fun highlightOccurrences(textPane: JTextPane) {
        val doc = textPane.styledDocument
        val text = doc.getText(0, doc.length)

        // Clear previous highlighting
        doc.setCharacterAttributes(0, doc.length, SimpleAttributeSet(), true)

        var pos = 0

        for (pair in KEYWORDS) {
            val word = pair.first
            val color = pair.second
            while (pos < text.length) {
                pos = text.indexOf(word, pos)
                if (pos < 0) break

                val attr = SimpleAttributeSet()
                StyleConstants.setForeground(attr, color)
                doc.setCharacterAttributes(pos, word.length, attr, false)

                pos += word.length
            }
        }
        textPane.foreground = Color.DARK_GRAY
    }

    fun createOutputTextPane(): JTextPaneWithHeader {
        val outputTextPane = JTextPaneWithHeader("Output:")
        outputTextPane.textPane.isEditable = false
        outputTextPane.textPane.foreground = Color.DARK_GRAY

        val padding = 10
        outputTextPane.setBorder(EmptyBorder(padding, padding, padding, padding))

        setFont(outputTextPane.textPane)

        setTabSize(outputTextPane.textPane, 4)

        return outputTextPane
    }

    private fun setFont(textPane: JTextPane) {
        try {
            var customFont = Font.createFont(Font.TRUETYPE_FONT, File("fonts/JetBrainsMono-Regular.ttf"))
            customFont = customFont.deriveFont(Font.PLAIN, 14f) // Set font size (adjust as needed)
            textPane.setFont(customFont)
        } catch (e: FontFormatException) {
            e.printStackTrace()
            // Handle exception if font loading fails
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    private fun createTriangleButton(): JButton {
        val button = JButton()
        button.icon = TriangleIcon(Color.decode("#008000"), 10, 10)
        button.isBorderPainted = false
        button.isFocusPainted = false
        button.isContentAreaFilled = false
        button.addActionListener {

            timer.start()
            progressBar.setStringPainted(true);

            button.isEnabled = false
            outputTextPane.textPane.text = ""

            val worker = object : SwingWorker<Unit, String>() {
                override fun doInBackground() {
                    val process = scriptExecutor.runKotlinScript(codeTextPane.textPane.text)
                    val reader = BufferedReader(InputStreamReader(process.inputStream))

                    while (true) {
                        val line = reader.readLine() ?: break
                        publish(line)
                    }
                    publish("Exit code: ${process.waitFor()}")
                }

                override fun process(chunks: MutableList<String>) {
                    for (chunk in chunks) {
                        appendToPane(outputTextPane.textPane, chunk + "\n", Color.BLACK)
                    }
                }

                override fun done() {
                    button.isEnabled = true
                    timer.stop()
                }
            }

            worker.execute()

        }
        return button
    }

    private class TriangleIcon(private val color: Color, private val width: Int, private val height: Int) : Icon {
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

            // Create header label
            val headerLabel = JLabel(headerText)
            headerLabel.horizontalAlignment = SwingConstants.CENTER
            add(headerLabel, BorderLayout.NORTH)

            // Create text pane
            textPane = JTextPane()
            val scrollPane = JScrollPane(textPane)
            add(scrollPane, BorderLayout.CENTER)
        }
    }

    fun appendToPane(textPane: JTextPane, text: String, color: Color = Color.DARK_GRAY) {
        val styleContext = StyleContext.getDefaultStyleContext()
        val attributeSet = styleContext.addAttribute(SimpleAttributeSet.EMPTY, StyleConstants.Foreground, color)

        val length = textPane.document.length
        textPane.document.insertString(length, text, attributeSet)
    }
}

@OptIn(ExperimentalPathApi::class)
fun main() {
    if (Files.exists(Paths.get("./.pdd.script"))) {
        Paths.get("./.pdd.script").deleteRecursively()
    }
    Files.createDirectory(Paths.get("./.pdd.script"))

    App()
}
