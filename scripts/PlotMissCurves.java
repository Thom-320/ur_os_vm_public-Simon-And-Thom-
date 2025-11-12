import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.*;
import javax.imageio.ImageIO;

public class PlotMissCurves {
    public static void main(String[] args) throws Exception {
        if(args.length < 2){
            System.err.println("Usage: java PlotMissCurves <input.csv> <output.png>");
            return;
        }
        File csv = new File(args[0]);
        File out = new File(args[1]);
        Map<String, TreeMap<Integer, Integer>> data = new LinkedHashMap<>();
        try(BufferedReader br = new BufferedReader(new FileReader(csv))){
            String header = br.readLine();
            if(header == null){ System.err.println("Empty CSV"); return; }
            String[] cols = header.split(",");
            int idxPolicy=-1, idxFrames=-1, idxFaults=-1;
            for(int i=0;i<cols.length;i++){
                String c = cols[i].trim().toLowerCase();
                if(c.equals("policy")) idxPolicy = i;
                else if(c.equals("frames")) idxFrames = i;
                else if(c.equals("faults") || c.equals("vm_faults")) idxFaults = i;
            }
            // Fallback to legacy schema policy,frames,vm_accesses,vm_faults,...
            if(idxPolicy < 0) idxPolicy = 0;
            if(idxFrames < 0) idxFrames = 1;
            if(idxFaults < 0) idxFaults = 3;
            String line;
            while((line = br.readLine()) != null){
                if(line.trim().isEmpty()) continue;
                String[] t = line.split(",");
                if(t.length <= Math.max(idxPolicy, Math.max(idxFrames, idxFaults))) continue;
                String policy = t[idxPolicy].trim();
                int frames = Integer.parseInt(t[idxFrames].trim());
                int faults = Integer.parseInt(t[idxFaults].trim());
                data.computeIfAbsent(policy, k -> new TreeMap<>()).put(frames, faults);
            }
        }
        int width = 900, height = 520;
        int left = 70, right = 20, top = 30, bottom = 60;
        BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setColor(Color.WHITE); g.fillRect(0,0,width,height);
        // Axes
        int x0 = left, y0 = height - bottom, x1 = width - right, y1 = top;
        g.setColor(Color.BLACK);
        g.drawLine(x0, y0, x1, y0);
        g.drawLine(x0, y0, x0, y1);
        // Find ranges
        int minF = Integer.MAX_VALUE, maxF = Integer.MIN_VALUE, maxFaults = 0;
        for(var e: data.values()){
            for(var kv: e.entrySet()){
                minF = Math.min(minF, kv.getKey());
                maxF = Math.max(maxF, kv.getKey());
                maxFaults = Math.max(maxFaults, kv.getValue());
            }
        }
        if(minF == Integer.MAX_VALUE){
            System.err.println("No data");
            return;
        }
        // Ticks
        g.setFont(new Font("SansSerif", Font.PLAIN, 12));
        int xw = x1 - x0, yh = y0 - y1;
        for(int f=minF; f<=maxF; f++){
            int x = x0 + (int)((f - minF) * (xw*1.0/(maxF - minF))); g.drawLine(x, y0, x, y0+4);
            String s = Integer.toString(f); int sw = g.getFontMetrics().stringWidth(s);
            g.drawString(s, x - sw/2, y0 + 18);
        }
        int yTicks = 6;
        for(int i=0;i<=yTicks;i++){
            int y = y0 - (int)(i * (yh*1.0/yTicks)); g.drawLine(x0-4, y, x0, y);
            int val = (int)Math.round(i * (maxFaults*1.0/yTicks));
            String s = Integer.toString(val); int sw = g.getFontMetrics().stringWidth(s);
            g.drawString(s, x0 - 8 - sw, y+4);
        }
        g.drawString("Frames (F)", (x0+x1)/2 - 30, height - 20);
        g.drawString("Page Faults", 10, (y0+y1)/2);
        // Colors
        Color[] colors = { Color.RED, new Color(0,128,0), Color.BLUE, Color.MAGENTA, Color.ORANGE };
        int ci=0;
        // Legend
        int lx = x1 - 150, ly = y1 + 10;
        for(String pol: data.keySet()){
            g.setColor(colors[ci % colors.length]);
            g.fillRect(lx, ly + ci*18, 12, 12);
            g.setColor(Color.BLACK);
            g.drawString(pol, lx + 18, ly + 10 + ci*18);
            ci++;
        }
        // Lines
        ci=0;
        for(var e: data.entrySet()){
            g.setStroke(new BasicStroke(2f));
            g.setColor(colors[ci % colors.length]);
            int prevX=-1, prevY=-1;
            for(var kv: e.getValue().entrySet()){
                int f = kv.getKey(); int faults = kv.getValue();
                int x = x0 + (int)((f - minF) * (xw*1.0/(maxF - minF)));
                int y = y0 - (int)(faults * (yh*1.0/Math.max(1,maxFaults)));
                g.fillOval(x-3,y-3,6,6);
                if(prevX!=-1){ g.drawLine(prevX,prevY,x,y); }
                prevX=x; prevY=y;
            }
            ci++;
        }
        g.dispose();
        out.getParentFile().mkdirs();
        ImageIO.write(img, "PNG", out);
        System.out.println("Wrote figure: "+ out.getAbsolutePath());
    }
}
