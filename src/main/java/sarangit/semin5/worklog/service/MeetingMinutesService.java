package sarangit.semin5.worklog.service;

import lombok.RequiredArgsConstructor;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType0Font;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import sarangit.semin5.worklog.entity.department;
import sarangit.semin5.worklog.entity.major_category;
import sarangit.semin5.worklog.entity.minor_category;
import sarangit.semin5.worklog.entity.request;
import sarangit.semin5.worklog.repository.DepartmentRepository;
import sarangit.semin5.worklog.repository.MajorCategoryRepository;
import sarangit.semin5.worklog.repository.MinorCategoryRepository;
import sarangit.semin5.worklog.repository.RequestRepository;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MeetingMinutesService {
    private static final Color NAVY = new Color(12, 47, 82);
    private static final Color BLUE = new Color(31, 105, 180);
    private static final Color PALE = new Color(241, 246, 251);
    private static final Color LINE = new Color(185, 201, 216);
    private static final Color TEXT = new Color(36, 50, 66);
    private final RequestRepository requests;
    private final MajorCategoryRepository majors;
    private final MinorCategoryRepository minors;
    private final DepartmentRepository departments;
    @Value("${worklog.pdf.font-path:C:/Windows/Fonts/malgun.ttf}") private String fontPath;

    public byte[] generate(MeetingMinutes minutes) {
        try (PDDocument document = new PDDocument(); ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            PDFont font = PDType0Font.load(document, new File(fontPath));
            File boldFile = new File(new File(fontPath).getParentFile(), "malgunbd.ttf");
            PDFont boldFont = boldFile.isFile() ? PDType0Font.load(document, boldFile) : font;
            Page page = new Page(document, font, boldFont);
            page.title("\ud68c\uc758\ub85d");
            page.section("1. \ud68c\uc758 \uac1c\uc694");
            page.overview(minutes);
            page.section("2. \uc54c\ub9bc\uc0ac\ud56d");
            page.notice(minutes.notice());
            page.section("3. \uc5c5\ubb34 \ub0b4\uc6a9");

            Map<Integer, String> majorNames = majors.findAll().stream().collect(Collectors.toMap(major_category::getId, major_category::getName));
            Map<Integer, String> minorNames = minors.findAll().stream().collect(Collectors.toMap(minor_category::getId, minor_category::getName));
            Map<Integer, String> departmentNames = departments.findAll().stream().collect(Collectors.toMap(department::getId, department::getName));
            List<request> items = requests.findAll().stream()
                    .filter(item -> minutes.meetingDate().equals(item.getProcessing_date())).toList();
            page.workHeader();
            if (items.isEmpty()) page.emptyWork();
            for (request item : items) page.workRow(new String[]{
                    value(majorNames.get(item.getMajor_category())), value(minorNames.get(item.getMinor_category())),
                    value(departmentNames.get(item.getDepartment())), value(item.getRequest_content()), value(item.getProcessing_content())
            });
            page.close();
            document.save(output);
            return output.toByteArray();
        } catch (IOException exception) {
            throw new IllegalStateException("PDF generation failed", exception);
        }
    }

    private static String value(String text) { return text == null || text.isBlank() ? "-" : text; }
    private static String names(List<Attendee> attendees, boolean present) {
        return attendees.stream().filter(attendee -> attendee.present() == present).map(Attendee::name).collect(Collectors.joining(", "));
    }
    private static String absenceReasons(List<Attendee> attendees) {
        String result = attendees.stream().filter(attendee -> !attendee.present())
                .map(attendee -> attendee.reason() == null || attendee.reason().isBlank() ? "\uc5f0\ucc28" : attendee.reason())
                .collect(Collectors.joining(", "));
        return result.isBlank() ? "-" : result;
    }

    public record MeetingMinutes(LocalDate meetingDate, String author, List<Attendee> attendees, String notice) { }
    public record Attendee(String name, boolean present, String reason) { }

    private static final class Page {
        private static final float LEFT = 36, WIDTH = 523, BOTTOM = 30;
        private final PDDocument document;
        private final PDFont font;
        private final PDFont boldFont;
        private PDPageContentStream stream;
        private float y;

        Page(PDDocument document, PDFont font, PDFont boldFont) throws IOException { this.document = document; this.font = font; this.boldFont = boldFont; nextPage(); }

        void title(String text) throws IOException {
            box(LEFT, y - 24, WIDTH, 22, NAVY, NAVY);
            centeredBold(text, LEFT, y - 16, WIDTH, 13, Color.WHITE);
            y -= 30;
        }
        void section(String text) throws IOException {
            ensure(28);
            boldText(text, LEFT, y - 13, 11, NAVY);
            box(LEFT, y - 20, 27, 2, BLUE, BLUE);
            y -= 29;
        }
        void overview(MeetingMinutes minutes) throws IOException {
            String[][] rows = {
                    {"\uc77c\uc2dc", minutes.meetingDate().format(DateTimeFormatter.ofPattern("yyyy\ub144MM\uc6d4dd\uc77c")), "\ubd80\uc11c", "IT\ud300"},
                    {"\uc791\uc131\uc790", value(minutes.author()), "\ucc38\uc11d\uc790", value(names(minutes.attendees(), true))},
                    {"\uacb0\uc11d\uc790", value(names(minutes.attendees(), false)), "\uacb0\uc11d \uc0ac\uc720", absenceReasons(minutes.attendees())}
            };
            float[] widths = {68, 193, 83, 179};
            for (String[] row : rows) {
                float height = Math.max(18, Math.max(maxLines(row[1], widths[1] - 10, 8), maxLines(row[3], widths[3] - 10, 8)) * 9 + 6);
                ensure(height);
                float x = LEFT;
                for (int i = 0; i < row.length; i++) {
                    boolean label = i % 2 == 0;
                    box(x, y - height, widths[i], height, label ? NAVY : Color.WHITE, LINE);
                    if (label) centeredBold(row[i], x, y - height / 2 - 3, widths[i], 8, Color.WHITE);
                    else centeredCell(row[i], x + 5, y, widths[i] - 10, height, 8, TEXT, 9);
                    x += widths[i];
                }
                y -= height;
            }
            y -= 8;
        }
        void notice(String notice) throws IOException {
            List<String> lines = wrap(value(notice), WIDTH - 18, 8);
            float height = Math.max(28, lines.size() * 9 + 8);
            ensure(height);
            box(LEFT, y - height, WIDTH, height, PALE, LINE);
            centeredCell(value(notice), LEFT + 9, y, WIDTH - 18, height, 8, TEXT, 9);
            y -= height + 8;
        }
        void workHeader() throws IOException {
            ensure(19);
            workHeaderAt(y);
            y -= 19;
        }
        void emptyWork() throws IOException {
            ensure(30);
            box(LEFT, y - 30, WIDTH, 30, Color.WHITE, LINE);
            centered("\ud574\ub2f9 \ub0a0\uc9dc\uc5d0 \ucc98\ub9ac \uc644\ub8cc\ub41c \uc5c5\ubb34\uac00 \uc5c6\uc2b5\ub2c8\ub2e4.", LEFT, y - 19, WIDTH, 8, TEXT);
            y -= 30;
        }
        void workRow(String[] values) throws IOException {
            float[] widths = {98, 74, 57, 143, 151};
            float height = 18;
            for (int i = 0; i < values.length; i++) height = Math.max(height, maxLines(values[i], widths[i] - 10, 8) * 9 + 6);
            if (y - height < BOTTOM) { nextPage(); workHeader(); }
            float x = LEFT;
            for (int i = 0; i < values.length; i++) {
                box(x, y - height, widths[i], height, Color.WHITE, LINE);
                centeredCell(values[i], x + 5, y, widths[i] - 10, height, 8, TEXT, 9);
                x += widths[i];
            }
            y -= height;
        }
        private void workHeaderAt(float top) throws IOException {
            String[] headers = {"\ub300\ubd84\ub958", "\uc18c\ubd84\ub958", "\ubd80\uc11c", "\uc694\uccad \ub0b4\uc6a9", "\ucc98\ub9ac\ub0b4\uc6a9"};
            float[] widths = {98, 74, 57, 143, 151};
            float x = LEFT;
            for (int i = 0; i < headers.length; i++) {
                box(x, top - 19, widths[i], 19, NAVY, NAVY);
                centeredBold(headers[i], x, top - 14, widths[i], 8, Color.WHITE);
                x += widths[i];
            }
        }
        private void ensure(float needed) throws IOException { if (y - needed < BOTTOM) nextPage(); }
        private void nextPage() throws IOException {
            if (stream != null) stream.close();
            document.addPage(new PDPage(PDRectangle.A4));
            stream = new PDPageContentStream(document, document.getPage(document.getNumberOfPages() - 1));
            y = 810;
        }
        private void box(float x, float bottom, float width, float height, Color fill, Color border) throws IOException {
            stream.setNonStrokingColor(fill); stream.addRect(x, bottom, width, height); stream.fill();
            stream.setStrokingColor(border); stream.setLineWidth(0.65f); stream.addRect(x, bottom, width, height); stream.stroke();
        }
        private void text(String value, float x, float baseline, float size, Color color) throws IOException {
            write(font, value, x, baseline, size, color);
        }
        private void boldText(String value, float x, float baseline, float size, Color color) throws IOException { write(boldFont, value, x, baseline, size, color); }
        private void write(PDFont typeface, String value, float x, float baseline, float size, Color color) throws IOException {
            stream.beginText(); stream.setFont(typeface, size); stream.setNonStrokingColor(color); stream.newLineAtOffset(x, baseline); stream.showText(value); stream.endText();
        }
        private void centered(String value, float x, float baseline, float width, float size, Color color) throws IOException {
            float textWidth = font.getStringWidth(value) / 1000 * size;
            text(value, x + Math.max(4, (width - textWidth) / 2), baseline, size, color);
        }
        private void centeredBold(String value, float x, float baseline, float width, float size, Color color) throws IOException {
            float textWidth = boldFont.getStringWidth(value) / 1000 * size;
            boldText(value, x + Math.max(4, (width - textWidth) / 2), baseline, size, color);
        }
        private void cell(String value, float x, float top, float width, float size, Color color, float leading) throws IOException {
            float baseline = top;
            for (String line : wrap(value, width, size)) { text(line, x, baseline, size, color); baseline -= leading; }
        }
        private void centeredCell(String value, float x, float top, float width, float height, float size, Color color, float leading) throws IOException {
            List<String> lines = wrap(value, width, size);
            float baseline = top - height / 2 - size * .35f + (lines.size() - 1) * leading / 2;
            for (String line : lines) {
                float textWidth = font.getStringWidth(line) / 1000 * size;
                text(line, x + Math.max(0, (width - textWidth) / 2), baseline, size, color);
                baseline -= leading;
            }
        }
        private int maxLines(String value, float width, float size) throws IOException { return wrap(value, width, size).size(); }
        private List<String> wrap(String value, float width, float size) throws IOException {
            String source = value == null || value.isBlank() ? "-" : value;
            java.util.ArrayList<String> lines = new java.util.ArrayList<>();
            for (String paragraph : source.replace("\r", "").split("\n", -1)) {
                StringBuilder line = new StringBuilder();
                for (int offset = 0; offset < paragraph.length();) {
                    int codePoint = paragraph.codePointAt(offset);
                    String character = new String(Character.toChars(codePoint));
                    if (line.length() > 0 && font.getStringWidth(line + character) / 1000 * size > width) { lines.add(line.toString()); line = new StringBuilder(); }
                    line.append(character); offset += Character.charCount(codePoint);
                }
                lines.add(line.isEmpty() ? "-" : line.toString());
            }
            return lines;
        }
        void close() throws IOException { if (stream != null) stream.close(); }
    }
}
