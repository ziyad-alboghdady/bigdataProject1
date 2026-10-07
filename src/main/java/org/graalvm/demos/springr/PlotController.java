package org.graalvm.demos.springr;

import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.Source;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.io.IOException;
import java.util.function.Function;

@RestController
public class PlotController {

    @Value("classpath:plot.R")
    private Resource rSource;

    private final DataService dataService;
    private Context context;
    private Function<DataHolder, String> plotFunction;

    public PlotController(DataService dataService) {
        this.dataService = dataService;
    }

    @PostConstruct
    @SuppressWarnings("unchecked")
    public void init() throws IOException {
        context = Context.newBuilder("R").allowAllAccess(true).build();
        Source source = Source.newBuilder("R", rSource.getURL()).build();
        plotFunction = context.eval(source).as(Function.class);
    }

    @GetMapping(value = "/plot", produces = "image/svg+xml")
    public ResponseEntity<String> plot() {
        double value = dataService.next();              // fetch one value from MongoDB

        String svg;
        synchronized (this) {                           // R context: one thread at a time
            svg = plotFunction.apply(new DataHolder(value));
        }

        return ResponseEntity.ok()
                .header("Refresh", "1")                 // browser reloads every second
                .body(svg);
    }

    @PreDestroy
    public void close() {
        if (context != null) context.close();
    }
}