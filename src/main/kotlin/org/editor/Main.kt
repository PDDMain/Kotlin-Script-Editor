package org.editor

import org.editor.components.JTextPaneWithHeader
import org.editor.components.TriangleIcon
import org.editor.components.appendToPane
import org.editor.components.setTabSize
import org.editor.errorparsing.parseErrors
import org.editor.styles.*
import org.editor.timestat.TimeStatistics
import org.executor.ScriptExecutor
import java.awt.*
import java.io.BufferedReader
import java.io.File
import java.io.IOException
import java.io.InputStreamReader
import java.nio.file.Files
import java.nio.file.Paths
import javax.swing.*
import javax.swing.border.EmptyBorder
import javax.swing.border.LineBorder
import javax.swing.event.DocumentEvent
import javax.swing.event.DocumentListener
import javax.swing.event.HyperlinkEvent
import javax.swing.filechooser.FileNameExtensionFilter
import javax.swing.plaf.basic.BasicProgressBarUI
import javax.swing.plaf.basic.BasicSplitPaneDivider
import javax.swing.text.*
import javax.swing.text.html.HTMLDocument
import javax.swing.text.html.HTMLEditorKit
import kotlin.io.path.ExperimentalPathApi
import kotlin.io.path.deleteRecursively


private val KEYWORDS = setOf(
    "val",
    "var",
    "fun",
    "while",
    "if",
    "for",
    "true",
    "false",
).map { Pair(it, KEYWORDS_ORANGE) }

class App {
    private val textEditorFrame: JFrame
    private val codeTextPane: JTextPaneWithHeader
    private val outputTextPane: JTextPaneWithHeader
    private var progressBar: JProgressBar
    private val runtimeTimer: Timer
    private val timeStatistics = TimeStatistics()
    private var runtimeTimerCurrentTime: Long = 0


    private val scriptExecutor = ScriptExecutor(File(".pdd.script/script.kts"))

    init {
        textEditorFrame = createTextEditorFrame()

        codeTextPane = createCodeTextPane()
        outputTextPane = createOutputTextPane()

        val splitPane = JSplitPane(JSplitPane.VERTICAL_SPLIT, codeTextPane, outputTextPane)
        splitPane.resizeWeight = 0.7
        val divider = splitPane.getComponent(2) as BasicSplitPaneDivider
        divider.background = BACKGROUND_DARK

        splitPane.background = Color.DARK_GRAY
        splitPane.border = LineBorder(OUTPUT_FONT_BLACK)
        splitPane.background = OUTPUT_FONT_BLACK

        textEditorFrame.add(splitPane)

        progressBar = createProgressBar()
        runtimeTimer = createTimer()

        textEditorFrame.jMenuBar = createMenuBar()

        textEditorFrame.isVisible = true
    }

    private fun createTimer(): Timer = Timer(100) {
        runtimeTimerCurrentTime += 100
        if (progressBar.value.toLong() * timeStatistics.getExpectedTime() < runtimeTimerCurrentTime * 100) {
            progressBar.value = (runtimeTimerCurrentTime * 100 / timeStatistics.getExpectedTime()).toInt()
        }
    }

    private fun createTextEditorFrame(): JFrame {
        val textEditorFrame = JFrame("Text Editor")
        textEditorFrame.defaultCloseOperation = JFrame.EXIT_ON_CLOSE
        textEditorFrame.setSize(1080, 800)
        textEditorFrame.setLocationRelativeTo(null)
        textEditorFrame.background = OUTPUT_FONT_BLACK
        return textEditorFrame
    }

    private fun createMenuBar(): JMenuBar {
        val menuBar = JMenuBar()
        menuBar.background = BACKGROUND_MENU_BAR

        val menuFile = JMenu("File")
        menuFile.foreground = FOREGROUND_LIGHT

        val menuItemNEW = JMenuItem("New")
        val menuItemOPEN = JMenuItem("Open")
        val menuItemSAVE = JMenuItem("Save")
        val menuItemQUIT = JMenuItem("Quit")

        menuBar.add(menuFile)

        menuFile.add(menuItemNEW)
        menuFile.add(menuItemOPEN)
        menuFile.add(menuItemSAVE)
        menuFile.add(menuItemQUIT)

        menuItemNEW.addActionListener { onNewClicked() }
        menuItemOPEN.addActionListener { onOpenClicked() }
        menuItemSAVE.addActionListener { onSaveClicked() }
        menuItemQUIT.addActionListener { onQuitClicked() }

        val triangleButton: JButton = createTriangleButton()
        menuBar.add(Box.createHorizontalGlue())

        menuBar.add(progressBar)
        menuBar.add(triangleButton)
        return menuBar
    }

    private fun onNewClicked() {
        codeTextPane.textPane.text = ""
    }

    private fun onOpenClicked() {
        val fileChooser = JFileChooser()
        val filter = FileNameExtensionFilter("Text Files", "txt")
        fileChooser.fileFilter = filter

        val returnValue = fileChooser.showOpenDialog(textEditorFrame)
        if (returnValue == JFileChooser.APPROVE_OPTION) {
            val selectedFile = fileChooser.selectedFile
            try {
                val fileContent = selectedFile.reader().readText()
                codeTextPane.textPane.text = fileContent
            } catch (e: Exception) {
                e.printStackTrace()
                JOptionPane.showMessageDialog(
                    textEditorFrame,
                    "Error reading the file.",
                    "Error",
                    JOptionPane.ERROR_MESSAGE
                )
            }
        }
    }

    private fun onSaveClicked() {
        val fileChooser = JFileChooser()
        val filter = FileNameExtensionFilter("Text Files", "txt")
        fileChooser.fileFilter = filter

        val returnValue = fileChooser.showSaveDialog(textEditorFrame)
        if (returnValue == JFileChooser.APPROVE_OPTION) {
            val selectedFile = fileChooser.selectedFile
            try {
                val writer = selectedFile.writer()
                writer.write(codeTextPane.textPane.text)
                writer.close()
            } catch (e: Exception) {
                e.printStackTrace()
                JOptionPane.showMessageDialog(
                    textEditorFrame,
                    "Error saving the file.",
                    "Error",
                    JOptionPane.ERROR_MESSAGE
                )
            }
        }
    }

    private fun onQuitClicked() {
        textEditorFrame.dispose()
    }

    private fun createProgressBar(): JProgressBar {
        val progressBar = JProgressBar(0, 100)
        progressBar.isStringPainted = false
        progressBar.foreground = PROGRESS_BAR_GREEN
        progressBar.background = BACKGROUND_LOADER
        progressBar.setUI(BasicProgressBarUI())
        return progressBar
    }

    private fun createCodeTextPane(): JTextPaneWithHeader {
        val code = JTextPaneWithHeader("Kotlin Script:")
        code.textPane.text = ""
        code.textPane.background = BACKGROUND_DARK
        code.textPane.foreground = FOREGROUND_LIGHT
        code.textPane.caretColor = FOREGROUND_LIGHT

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
            }
        })
        return code
    }

    private fun highlightOccurrences(textPane: JTextPane) {
        val doc = textPane.styledDocument
        val text = doc.getText(0, doc.length)

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
        textPane.foreground = FOREGROUND_LIGHT
    }

    private fun createOutputTextPane(): JTextPaneWithHeader {
        val outputTextPane = JTextPaneWithHeader("Output:")
        outputTextPane.textPane.isEditable = false
        outputTextPane.textPane.foreground = FOREGROUND_LIGHT
        outputTextPane.textPane.background = BACKGROUND_DARK
        outputTextPane.textPane.caretColor = FOREGROUND_LIGHT

        val padding = 10
        outputTextPane.setBorder(EmptyBorder(padding, padding, padding, padding))

        setFont(outputTextPane.textPane)

        setTabSize(outputTextPane.textPane, 4)

        return outputTextPane
    }

    private fun setFont(textPane: JTextPane) {
        try {
            var customFont = Font.createFont(Font.TRUETYPE_FONT, File("fonts/JetBrainsMono-Regular.ttf"))
            customFont = customFont.deriveFont(Font.PLAIN, 14f)
            textPane.setFont(customFont)
        } catch (e: FontFormatException) {
            e.printStackTrace()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    private fun createTriangleButton(): JButton {
        val button = JButton()
        button.icon = TriangleIcon(TRIANGLE_BUTTON_GREEN, 10, 10)
        button.isBorderPainted = false
        button.isFocusPainted = false
        button.isContentAreaFilled = false
        button.addActionListener {
            runProgram(button)
        }
        return button
    }

    private fun runProgram(button: JButton) {
        progressBar.value = 0
        runtimeTimerCurrentTime = 0
        runtimeTimer.start()

        button.isEnabled = false
        button.icon = TriangleIcon(TRIANGLE_BUTTON_RED, 10, 10)
        outputTextPane.textPane.contentType = "text/plain"
        outputTextPane.textPane.text = ""

        val worker = createRuntimeWorker(button)

        worker.execute()
    }

    private fun createRuntimeWorker(button: JButton): SwingWorker<Unit, String> =
        object : SwingWorker<Unit, String>() {
            var exitCode = 0

            override fun doInBackground() {
                timeStatistics.startExecution()
                val process = scriptExecutor.runKotlinScript(codeTextPane.textPane.text)
                val reader = BufferedReader(InputStreamReader(process.inputStream))

                while (true) {
                    val line = reader.readLine() ?: break
                    publish(line)
                }
                exitCode = process.waitFor()
            }

            override fun process(chunks: MutableList<String>) {
                for (chunk in chunks) {
                    appendToPane(outputTextPane.textPane, chunk + "\n", FOREGROUND_OUTPUT_LIGHT)
                }
            }

            override fun done() {
                button.icon = TriangleIcon(TRIANGLE_BUTTON_GREEN, 10, 10)
                button.isEnabled = true
                appendToPane(outputTextPane.textPane, "Exit code: ${exitCode}\n", FOREGROUND_LIGHT)
                timeStatistics.endExecution()
                runtimeTimer.stop()
                progressBar.value = 100

                if (exitCode != 0) {
                    markErrors()
                }
            }
        }

    private fun markErrors() {

        val tokens = outputTextPane.textPane.text.lines().map { it.split(" ", limit = 2) }
        outputTextPane.textPane.contentType = "text/html"
        val doc: HTMLDocument = outputTextPane.textPane.document as HTMLDocument
        val editorKit: HTMLEditorKit = outputTextPane.textPane.editorKit as HTMLEditorKit


        editorKit.insertHTML(doc, doc.length, "<body style='background-color: #333; color: #FFF;'>", 0, 0, null)
        val listOfErrors = parseErrors(tokens)
        var listOfErrorsIndex = 0
        for (lineIndex in tokens.indices) {
            val newLine =
                if (listOfErrorsIndex < listOfErrors.size && lineIndex == listOfErrors[listOfErrorsIndex].lineInOutput) {
                    "<a href=\"${listOfErrors[listOfErrorsIndex].lineInCode},${listOfErrors[listOfErrorsIndex].symbolInCode}\" style=\"color: #FF0;\">${tokens[lineIndex][0]}</a> " + tokens[lineIndex].takeLast(
                        tokens[lineIndex].size - 1
                    )
                        .reduce { acc, s -> acc + s }.also { listOfErrorsIndex++ }
                } else {
                    tokens[lineIndex].reduce { acc, s -> acc + s }
                }
            editorKit.insertHTML(doc, doc.length, newLine + "\n", 0, 0, null)
        }
        editorKit.insertHTML(doc, doc.length, "</body>", 0, 0, null)

        outputTextPane.textPane.addHyperlinkListener { e ->
            if (e.eventType == HyperlinkEvent.EventType.ACTIVATED) {
                val desc = e.description.split(",")
                val line = desc[0].toInt()
                val symbol = desc[1].toInt()
                codeTextPane.textPane.requestFocus()
                val lines = codeTextPane.textPane.text.lines()
                val lineStartOffset = lines.take(line).sumOf { it.length } + line
                val position = lineStartOffset + symbol
                codeTextPane.textPane.caretPosition = position
            }
        }
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
