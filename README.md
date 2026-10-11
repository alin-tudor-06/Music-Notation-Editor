# Music Notation

A desktop music notation editor built from scratch in **Java 21 + JavaFX**.

The application supports reading and writing MusicXML, real-time MIDI playback, and interactive score editing — all implemented without any external notation, XML, or audio libraries.

---

## Features

### Music Notation Rendering
- Custom-built layout engine with **measure spacing**, **system breaking**, and **horizontal justification**
- Full **SMuFL / Bravura** font integration for all musical symbols
- Automatic beams, slurs, ties, and tuplets rendering
- Multi-system score with vertical scroll

### MusicXML I/O
- **Export** to MusicXML 4.0 (validated in MuseScore Studio)
- **Import** from MusicXML (`.musicxml`, `.xml`) and compressed MusicXML (`.mxl`)
- Round-trip safe: export → import → identical score

Supported elements:
- Notes, rests, durations (whole → 16th)
- Pitches with accidentals (sharp, flat, natural)
- Clef, key, and time signature changes
- Ties, slurs
- Articulations (staccato, staccatissimo, accent, marcato, tenuto, fermata)
- Dynamics (`ppp` → `fff`)
- Hairpins (crescendo, decrescendo)
- Tempo markings (text + BPM)
- Text expressions (dolce, cantabile, …)
- Repeat signs (start / end)
- Tuplets (triplets, quintuplets, …)

### Playback
- Real-time **MIDI playback** using `javax.sound.midi`
- **Repeat sign awareness** (plays repeats in the correct order)
- Key signature applied during playback (F# → F# sound, not F natural)
- Adjustable **volume** and **tempo** sliders
- **Live cursor** highlighting the currently playing note

### Interactive Editor
- **Categorized palette**: Notes, Rests, Accidentals, Articulations, Dynamics, Connections, Hairpins, Signs
- **Click-to-place** notes on the staff (mouse-based)
- **Hover ghost preview** — see the note before placing it
- **Click to select**, `Delete` to remove
- **Toggle behavior** for accidentals, articulations, dynamics, repeat signs
- **2-click flow** for ties, slurs, hairpins
- **Attribute editor** for clef / key / time per measure
- **Auto-extend**: new measure created automatically when the current one is full

### User Interface
- **File menu**: New / Open / Save / Save As / Exit
- **Playback menu**: toggle playback toolbar
- **Category toolbar + dynamic sub-toolbar**: clean, scalable palette design
- **Attributes toolbar**: clef / key / time dropdowns
- **Status bar**: file name, measure count, active mode, selection state

---

## Architecture

The project follows a **clean, layered architecture** that separates model, layout, rendering, and I/O:

**Model** → **Layout** → **Renderer** → **SMuFL**

- **Model** – `Score → Measure → Note, Tuplet, Slur` (pure musical data)
- **Layout** – `MeasureSpacingEngine, SystemBreaker, MeasureJustificationEngine` (spacing, breaking, scaling)
- **Renderer** – `NoteRenderer, BeamRenderer, SlurRenderer, TieRenderer, ArticulationRenderer` (JavaFX Canvas drawing)
- **SMuFL** – `BravuraFont, SmuflGlyph` (Bravura font + glyph mapping)

---

## Package Structure

```
com.alin.musicnotation
├── model/                 – musical data model
│   ├── articulation/      – Articulation, ArticulationType
│   ├── clef/              – ClefType
│   ├── dynamic/           – Dynamic, DynamicType
│   ├── hairpin/           – Hairpin, HairpinType
│   ├── layout/            – NoteLayout, StemDirection
│   ├── repeat/            – BarlineType
│   ├── tempo/             – Tempo, TempoPlacement
│   └── text/              – TextExpression, TextPlacement
├── layout/                – spacing, system breaking, justification
├── renderer/              – Canvas drawing
├── smufl/                 – Bravura font + SMuFL glyphs
├── musicxml/
│   ├── common/            – reusable mappers (Duration, Pitch, Clef)
│   ├── export/            – MusicXML writer
│   └── imports/           – MusicXML parser
├── playback/              – MIDI sequence + player
├── editor/                – interactive editing
└── MusicNotationApp.java  – JavaFX Application
```

---

## Technologies

| Tech | Usage |
|------|-------|
| **Java 21** | Language + runtime |
| **JavaFX 21** | UI, Canvas rendering |
| **Maven** | Build system |
| **javax.sound.midi** | MIDI playback (no external deps) |
| **Bravura** | SMuFL music font |
| **JUnit 5** | Unit testing |
| **StAX** (`javax.xml.stream`) | MusicXML parsing/writing |

**Zero external dependencies** for XML, MIDI, or notation logic — everything is built from scratch.

---

## Getting Started

### Prerequisites
- JDK 21 or newer
- Maven 3.8+

### Build
```bash
git clone https://github.com/alin-tudor-06/notes_app.git
cd notes_app/notes
mvn clean package
```

### Run
```bash
mvn javafx:run
```

Or from IntelliJ:

- Open `MusicNotationApp.java`

- Right-click → Run

### Quick test

1. Launch the app → empty score appears
2. In the **Notes** category, click **♩**, then click on the staff → note added
3. Switch to **Accidentals** → click **♯** → click on a note → sharp applied
4. **Playback → Show Playback Controls** → **▶ Play**
5. **File → Save As** → export as MusicXML
6. Open the file in MuseScore to verify

---

## How to Use

### Adding Notes
1. In the **Notes** category, choose a duration (whole, half, quarter, eighth, sixteenth)
2. Move the mouse over the staff — a **gray ghost preview** appears
3. Click to place the note
4. When the measure is full, a new measure is created automatically

### Adding Rests
Switch to the **Rests** category and choose a rest type.

### Accidentals
1. Switch to **Accidentals**
2. Click **♯**, **♭**, or **♮**
3. Click on an existing note
4. Click the same button + note again to remove (toggle)

### Articulations & Dynamics
Same pattern: pick from the category, click on a note, toggle to remove.

### Ties & Slurs
1. Switch to **Connections**
2. Click **⌒** (tie) or **‿** (slur)
3. Click on the **start note** (highlighted with a colored ring)
4. Click on the **end note**
5. `ESC` to cancel

### Hairpins (Crescendo / Decrescendo)
Same 2-click flow as ties/slurs, from the **Hairpins** category.

### Tempo & Text Expressions
1. Switch to **Signs**
2. Select a measure first (click on empty space in the measure)
3. Click **♩= Tempo** or **Text** to open a dialog

### Repeat Signs
From **Signs**, click **‖:** or **:‖** to toggle on the selected measure.

### Attribute Changes (Clef / Key / Time)
1. Select a measure (click on empty space in the measure)
2. Use the **Attributes** toolbar dropdowns

### Playback
1. **Playback → Show Playback Controls** (checkbox)
2. Click **▶ Play**, adjust **Vol** / **Tempo**
3. The current note is highlighted with an orange circle

---

## Testing

Run all tests:

```bash
mvn test
```

Test suites cover round-trip export/import for every feature (notes, rests, ties, slurs, dynamics, articulations, tempo, repeats, hairpins, text, tuplets, accidentals, clef/key/time changes).

---

## Roadmap

### Completed
- Layout engine with spacing, system breaking, and justification
- SMuFL / Bravura font integration for all musical symbols
- MusicXML 4.0 export and import (with round-trip validation)
- MIDI playback with repeat sign awareness
- Interactive editor with categorized palette

### Planned
- Undo / Redo
- Keyboard shortcuts (Ctrl+S, Ctrl+O, Ctrl+Z)
- Zoom in / out
- Cross-measure ties and slurs
- Multiple voices per staff
- Multi-staff support (piano grand staff)
- Lyrics
- Ornaments (trill, turn, mordent)
- PDF / PNG export

---

## License

**All Rights Reserved.** Copyright (c) 2026 Tudor Constantin-Alin.

This project is **source-available for viewing and educational purposes only**. Commercial use, redistribution, or inclusion in other projects requires prior written permission.

See [LICENSE](LICENSE) for full terms.

---

## Author

**Tudor Constantin-Alin**

Built from scratch as a learning and portfolio project to explore the architecture behind professional music notation software.

---

## Acknowledgements

- [Bravura font](https://github.com/steinbergmedia/bravura) – SMuFL music font by Steinberg Media (SIL Open Font License)
- [SMuFL specification](https://www.smufl.org/) – Standard Music Font Layout
- [MusicXML specification](https://www.musicxml.com/) – W3C Community Group standard