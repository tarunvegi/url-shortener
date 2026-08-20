"""
Renders the Markdown sources in docs/ to the polished PDF deliverables at the repo root.
Usage: python docs/build_pdfs.py
Requires: reportlab (pip install reportlab)
"""
import os
import re
import sys

from reportlab.lib.pagesizes import LETTER
from reportlab.lib import colors
from reportlab.lib.styles import getSampleStyleSheet, ParagraphStyle
from reportlab.lib.units import inch
from reportlab.platypus import (
    SimpleDocTemplate, Paragraph, Spacer, Table, TableStyle,
    ListFlowable, ListItem, Preformatted, HRFlowable
)
from reportlab.lib.enums import TA_LEFT

DOCS_DIR = os.path.dirname(os.path.abspath(__file__))
ROOT_DIR = os.path.dirname(DOCS_DIR)

DOCS = [
    ("architecture.md", "ARCHITECTURE.pdf", "Architecture Overview"),
    ("ai_workflow.md", "AI_WORKFLOW.pdf", "AI Workflow Documentation"),
    ("scenario_1_greenfield.md", "SCENARIO_1_GREENFIELD.pdf", "Scenario 1 -- Greenfield"),
    ("scenario_2_brownfield.md", "SCENARIO_2_BROWNFIELD.pdf", "Scenario 2 -- Brownfield"),
    ("scenario_3_ambiguous.md", "SCENARIO_3_AMBIGUOUS.pdf", "Scenario 3 -- Ambiguous Requirement"),
    ("risks_and_tradeoffs.md", "RISKS_AND_TRADEOFFS.pdf", "Risks and Trade-offs"),
    ("final_engineering_summary.md", "FINAL_ENGINEERING_SUMMARY.pdf", "Final Engineering Summary"),
]

styles = getSampleStyleSheet()
styles.add(ParagraphStyle(name="H1Custom", parent=styles["Heading1"], fontSize=20,
                           spaceAfter=14, spaceBefore=6, textColor=colors.HexColor("#14213d")))
styles.add(ParagraphStyle(name="H2Custom", parent=styles["Heading2"], fontSize=14,
                           spaceAfter=8, spaceBefore=16, textColor=colors.HexColor("#1d3461")))
styles.add(ParagraphStyle(name="H3Custom", parent=styles["Heading3"], fontSize=11.5,
                           spaceAfter=6, spaceBefore=10, textColor=colors.HexColor("#2b4570")))
styles.add(ParagraphStyle(name="BodyCustom", parent=styles["BodyText"], fontSize=9.5,
                           leading=13.5, spaceAfter=6, alignment=TA_LEFT))
styles.add(ParagraphStyle(name="BulletCustom", parent=styles["BodyText"], fontSize=9.5,
                           leading=13.5, leftIndent=14))
styles.add(ParagraphStyle(name="TableCell", parent=styles["BodyText"], fontSize=8.3, leading=11))
styles.add(ParagraphStyle(name="TableHeader", parent=styles["BodyText"], fontSize=8.5,
                           leading=11, textColor=colors.white, fontName="Helvetica-Bold"))
code_style = ParagraphStyle(name="Code", fontName="Courier", fontSize=8, leading=10,
                             backColor=colors.HexColor("#f4f4f4"), leftIndent=8, spaceAfter=6, spaceBefore=2)


def inline(text):
    text = text.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;")
    text = re.sub(r"\*\*(.+?)\*\*", r"<b>\1</b>", text)
    text = re.sub(r"`(.+?)`", r'<font face="Courier" size="8.5">\1</font>', text)
    return text


def parse_table(lines, start):
    rows = []
    i = start
    while i < len(lines) and lines[i].strip().startswith("|"):
        row = lines[i]
        if re.match(r"^\|[\s:\-|]+\|$", row.strip()):
            i += 1
            continue
        cells = [c.strip() for c in row.strip().strip("|").split("|")]
        rows.append(cells)
        i += 1
    return rows, i


def build_story(md_text, title):
    story = []
    lines = md_text.split("\n")
    i = 0
    n = len(lines)
    while i < n:
        line = lines[i]
        stripped = line.strip()

        if not stripped:
            i += 1
            continue

        if stripped.startswith("```"):
            i += 1
            code_lines = []
            while i < n and not lines[i].strip().startswith("```"):
                code_lines.append(lines[i])
                i += 1
            i += 1
            story.append(Preformatted("\n".join(code_lines), code_style))
            continue

        if stripped == "---":
            story.append(HRFlowable(width="100%", thickness=0.6, color=colors.HexColor("#cccccc"),
                                     spaceBefore=10, spaceAfter=10))
            i += 1
            continue

        if stripped.startswith("# "):
            story.append(Paragraph(inline(stripped[2:]), styles["H1Custom"]))
            i += 1
            continue
        if stripped.startswith("## "):
            story.append(Paragraph(inline(stripped[3:]), styles["H2Custom"]))
            i += 1
            continue
        if stripped.startswith("### "):
            story.append(Paragraph(inline(stripped[4:]), styles["H3Custom"]))
            i += 1
            continue

        if stripped.startswith("|"):
            rows, i = parse_table(lines, i)
            if rows:
                header = [Paragraph(inline(c), styles["TableHeader"]) for c in rows[0]]
                body = [[Paragraph(inline(c), styles["TableCell"]) for c in r] for r in rows[1:]]
                table_data = [header] + body
                ncols = len(rows[0])
                avail_width = LETTER[0] - 1.4 * inch
                col_width = avail_width / ncols
                t = Table(table_data, colWidths=[col_width] * ncols, repeatRows=1)
                t.setStyle(TableStyle([
                    ("BACKGROUND", (0, 0), (-1, 0), colors.HexColor("#1d3461")),
                    ("GRID", (0, 0), (-1, -1), 0.5, colors.HexColor("#cccccc")),
                    ("VALIGN", (0, 0), (-1, -1), "TOP"),
                    ("ROWBACKGROUNDS", (0, 1), (-1, -1), [colors.white, colors.HexColor("#f7f7f9")]),
                    ("TOPPADDING", (0, 0), (-1, -1), 4),
                    ("BOTTOMPADDING", (0, 0), (-1, -1), 4),
                ]))
                story.append(t)
                story.append(Spacer(1, 8))
            continue

        m = re.match(r"^(\d+)\.\s+(.*)", stripped)
        if m:
            items = []
            while i < n and re.match(r"^\d+\.\s+", lines[i].strip()):
                txt = re.sub(r"^\d+\.\s+", "", lines[i].strip())
                items.append(ListItem(Paragraph(inline(txt), styles["BulletCustom"])))
                i += 1
            story.append(ListFlowable(items, bulletType="1", start="1", leftIndent=18))
            story.append(Spacer(1, 6))
            continue

        if stripped.startswith("- ") or stripped.startswith("* "):
            items = []
            while i < n and (lines[i].strip().startswith("- ") or lines[i].strip().startswith("* ")):
                txt = lines[i].strip()[2:]
                items.append(ListItem(Paragraph(inline(txt), styles["BulletCustom"])))
                i += 1
            story.append(ListFlowable(items, bulletType="bullet", start="circle", leftIndent=18))
            story.append(Spacer(1, 6))
            continue

        if stripped.startswith(">"):
            story.append(Paragraph(inline(stripped.lstrip("> ").strip()), styles["BulletCustom"]))
            i += 1
            continue

        story.append(Paragraph(inline(stripped), styles["BodyCustom"]))
        i += 1

    return story


def build_pdf(md_path, pdf_path, title):
    with open(md_path, "r", encoding="utf-8") as f:
        md_text = f.read()
    doc = SimpleDocTemplate(pdf_path, pagesize=LETTER,
                             leftMargin=0.7 * inch, rightMargin=0.7 * inch,
                             topMargin=0.7 * inch, bottomMargin=0.7 * inch,
                             title=title)
    story = build_story(md_text, title)
    doc.build(story)
    print(f"Built {pdf_path}")


def main():
    for md_name, pdf_name, title in DOCS:
        md_path = os.path.join(DOCS_DIR, md_name)
        pdf_path = os.path.join(ROOT_DIR, pdf_name)
        if not os.path.exists(md_path):
            print(f"SKIP (missing source): {md_path}")
            continue
        build_pdf(md_path, pdf_path, title)


if __name__ == "__main__":
    sys.exit(main())
