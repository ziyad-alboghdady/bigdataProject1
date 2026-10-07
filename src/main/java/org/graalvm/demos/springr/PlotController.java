package org.graalvm.demos.springr;

import org.bson.Document;
import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.Source;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

@Controller
public class PlotController {

    @Value("classpath:plot.R")
    private Resource rSource;

    @Autowired
    private MongoTemplate mongoTemplate;

    // Plain reference — not autowired to avoid circular dependency
    private Function<DataHolder, String> plotFunction;

    // Set to your assigned group column (e.g., "Col-1")
    private static final String TARGET_COLUMN = "Col-1";
    private final List<Double> dataQueue = new ArrayList<>();
    private int cursorIndex = 0;

    @PostConstruct
    public void init() {
        // 1. Fetch 100 values from MongoDB
        try {
            List<Document> docs = mongoTemplate.findAll(Document.class, "measurements");
            for (Document doc : docs) {
                Object raw = doc.get(TARGET_COLUMN);
                if (raw != null) {
                    dataQueue.add(Double.parseDouble(raw.toString()));
                }
            }
            System.out.println("Loaded " + dataQueue.size() + " data points for " + TARGET_COLUMN);
        } catch (Exception e) {
            System.err.println("Error reading from MongoDB: " + e.getMessage());
        }

        // 2. Initialize Polyglot GraalVM Context and compile R function
        try {
            Context ctx = Context.newBuilder("R")
                    .allowAllAccess(true)
                    .build();
            Source source = Source.newBuilder("R", rSource.getURL()).build();
            this.plotFunction = ctx.eval(source).as(Function.class);
            System.out.println("FastR plot function initialized successfully.");
        } catch (IOException e) {
            System.err.println("Error initializing FastR context: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @GetMapping(value = "/plot", produces = "image/svg+xml")
    public ResponseEntity<String> plot() {
        if (plotFunction == null) {
            return new ResponseEntity<>("<svg><text y='20'>FastR engine is not initialized</text></svg>", HttpStatus.INTERNAL_SERVER_ERROR);
        }

        if (dataQueue.isEmpty()) {
            return new ResponseEntity<>("<svg><text y='20'>No data found in MongoDB</text></svg>", HttpStatus.OK);
        }

        double nextVal;
        synchronized (this) {
            nextVal = dataQueue.get(cursorIndex);
            cursorIndex = (cursorIndex + 1) % dataQueue.size();
        }

        String svg;
        synchronized (plotFunction) {
            svg = plotFunction.apply(new DataHolder(nextVal));
        }

        // Send refresh header so browser auto-updates every 1 second
        HttpHeaders headers = new HttpHeaders();
        headers.set("Refresh", "1");

        return new ResponseEntity<>(svg, headers, HttpStatus.OK);
    }
}