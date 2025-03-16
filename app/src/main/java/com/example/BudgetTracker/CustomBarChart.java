package com.example.BudgetTracker;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;
import java.util.List;

public class CustomBarChart extends View {

    // Listen für Einnahmen, Ausgaben und die Labels (X-Achse)
    private List<Float> einnahmen; // Daten für Einnahmen
    private List<Float> ausgaben;  // Daten für Ausgaben
    private List<String> labels;  // Beschriftungen für die X-Achse

    // Paint-Objekte für die verschiedenen Zeichenstile
    private Paint paintEinnahmen; // Farbe und Stil für Einnahmen
    private Paint paintAusgaben;  // Farbe und Stil für Ausgaben
    private Paint paintText;      // Farbe und Stil für Texte (Labels, Werte)
    private Paint paintAxis;      // Farbe und Stil für die Achsen
    private Paint paintGrid;      // Farbe und Stil für Hintergrundlinien (Gitter)

    // Konstruktor
    public CustomBarChart(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(); // Initialisierung der Paint-Objekte
    }

    private void init() {
        // Initialisierung der Farben und Stile

        // Grün für Einnahmen
        paintEinnahmen = new Paint();
        paintEinnahmen.setColor(Color.parseColor("#4CAF50"));
        paintEinnahmen.setStyle(Paint.Style.FILL);

        // Rot für Ausgaben
        paintAusgaben = new Paint();
        paintAusgaben.setColor(Color.parseColor("#F44336"));
        paintAusgaben.setStyle(Paint.Style.FILL);

        // Schwarz für Texte (Labels und Werte)
        paintText = new Paint();
        paintText.setColor(Color.BLACK);
        paintText.setTextSize(40); // Schriftgröße
        paintText.setTextAlign(Paint.Align.CENTER); // Zentrierte Ausrichtung

        // Schwarz für Achsen
        paintAxis = new Paint();
        paintAxis.setColor(Color.BLACK);
        paintAxis.setStrokeWidth(4); // Breite der Achsen

        // Hellgrau für Hintergrundlinien
        paintGrid = new Paint();
        paintGrid.setColor(Color.LTGRAY);
        paintGrid.setStrokeWidth(2); // Dünne Linien
        paintGrid.setStyle(Paint.Style.STROKE);
    }

    // Methode zum Setzen der Daten für das Diagramm
    public void setData(List<Float> einnahmen, List<Float> ausgaben, List<String> labels) {
        // Validierung der Eingabedaten
        if (einnahmen == null || ausgaben == null || labels == null) {
            throw new IllegalArgumentException("Einnahmen, Ausgaben und Labels dürfen nicht null sein!");
        }
        if (einnahmen.size() != ausgaben.size() || einnahmen.size() != labels.size()) {
            throw new IllegalArgumentException("Einnahmen, Ausgaben und Labels müssen die gleiche Größe haben!");
        }

        // Zuweisung der Daten
        this.einnahmen = einnahmen;
        this.ausgaben = ausgaben;
        this.labels = labels;

        invalidate(); // Neuzeichnen des Diagramms
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        // Wenn keine Daten vorhanden sind, nichts zeichnen
        if (einnahmen == null || ausgaben == null || labels == null || einnahmen.isEmpty()) {
            return;
        }

        int width = getWidth();  // Breite der Ansicht
        int height = getHeight(); // Höhe der Ansicht
        int padding = 100; // Abstand zum Rand

        // Berechnung der Diagrammgröße
        int chartWidth = width - 2 * padding;
        int chartHeight = height - 2 * padding;

        // Breite der Balkengruppen
        int groupWidth = chartWidth / labels.size();
        int barWidth = groupWidth / 3;

        // Ermittlung des maximalen Wertes (für die Skalierung)
        float maxValue = Math.max(getMax(einnahmen), getMax(ausgaben));

        // Zeichnen der Hintergrundlinien (Gitter)
        int numGridLines = 5; // Anzahl der horizontalen Linien
        for (int i = 0; i <= numGridLines; i++) {
            float y = padding + (chartHeight / (float) numGridLines) * i;
            canvas.drawLine(padding, y, width - padding, y, paintGrid);

            // Y-Achsen-Beschriftung
            float value = maxValue - (maxValue / numGridLines) * i;
            canvas.drawText(String.valueOf((int) value), padding - 50, y + 10, paintText);
        }

        // Zeichnen der Achsen
        canvas.drawLine(padding, padding, padding, height - padding, paintAxis); // Y-Achse
        canvas.drawLine(padding, height - padding, width - padding, height - padding, paintAxis); // X-Achse

        // Zeichnen der Balken
        for (int i = 0; i < labels.size(); i++) {
            // Höhe der Balken relativ zur Diagrammhöhe
            float einnahmenHeight = (einnahmen.get(i) / maxValue) * chartHeight;
            float ausgabenHeight = (ausgaben.get(i) / maxValue) * chartHeight;

            // Position der Einnahmen-Balken
            float leftEinnahmen = padding + i * groupWidth + barWidth / 2;
            float rightEinnahmen = leftEinnahmen + barWidth;

            // Position der Ausgaben-Balken
            float leftAusgaben = rightEinnahmen + barWidth / 2;
            float rightAusgaben = leftAusgaben + barWidth;

            // Formatierte Zahlen mit 2 Dezimalstellen
            String formattedEinnahmen = String.format("%.2f", einnahmen.get(i));
            String formattedAusgaben = String.format("%.2f", ausgaben.get(i));

            // Zeichnen der Einnahmen-Balken
            canvas.drawRect(leftEinnahmen, height - padding - einnahmenHeight, rightEinnahmen, height - padding, paintEinnahmen);

            // Zeichnen der Ausgaben-Balken
            canvas.drawRect(leftAusgaben, height - padding - ausgabenHeight, rightAusgaben, height - padding, paintAusgaben);

            // Zeichnen der formatierten Werte oberhalb der Balken
            canvas.drawText(formattedEinnahmen, (leftEinnahmen + rightEinnahmen) / 2, height - padding - einnahmenHeight - 10, paintText);
            canvas.drawText(formattedAusgaben, (leftAusgaben + rightAusgaben) / 2, height - padding - ausgabenHeight - 10, paintText);

            // Labels unterhalb der X-Achse
            canvas.drawText(labels.get(i), (leftEinnahmen + rightAusgaben) / 2, height - padding + 50, paintText);
        }
    }

    // Hilfsmethode zum Ermitteln des maximalen Wertes in einer Liste
    private float getMax(List<Float> values) {
        float max = 0;
        for (Float value : values) {
            if (value > max) max = value;
        }
        return max;
    }
}
