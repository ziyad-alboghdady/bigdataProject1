package org.graalvm.demos.springr;

import org.bson.Document;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import java.util.concurrent.atomic.AtomicInteger;

@Service
public class DataService {

    private static final String COLLECTION = "pro1";
    private static final String COLUMN     = "Col-1";   // our group's column
    private static final int    ROW_COUNT  = 100;

    private final MongoTemplate mongoTemplate;
    private final AtomicInteger cursor = new AtomicInteger(0);

    public DataService(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    /** Fetches ONE value from MongoDB, then moves to the next row (1..100, then wraps). */
    public double next() {
        int i = cursor.getAndUpdate(v -> (v + 1) % ROW_COUNT);
        String row = "Row-" + (i + 1);

        Document doc = mongoTemplate.findOne(
                new Query(Criteria.where("Row").is(row)), Document.class, COLLECTION);

        if (doc == null) {
            throw new IllegalStateException(row + " not found in collection " + COLLECTION);
        }
        Number n = doc.get(COLUMN, Number.class);
        if (n == null) {
            throw new IllegalStateException(COLUMN + " missing in " + row);
        }
        return n.doubleValue();
    }
}