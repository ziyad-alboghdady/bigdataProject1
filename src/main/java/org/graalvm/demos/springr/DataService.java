package org.graalvm.demos.springr;

import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import java.util.concurrent.atomic.AtomicInteger;

@Service
public class DataService {

    public static final String COLLECTION = "pro1";
    public static final String COLUMN     = "Col-1";   // Group 1
    public static final int    ROW_COUNT  = 100;

    @Autowired
    private MongoTemplate mongoTemplate;

    private final AtomicInteger cursor = new AtomicInteger(0);

    /** Fetches one value from MongoDB and advances to the next row. */
    public double next() {
        int i = cursor.getAndUpdate(v -> (v + 1) % ROW_COUNT);

        Query query = new Query(Criteria.where("idx").is(i));
        Document doc = mongoTemplate.findOne(query, Document.class, COLLECTION);

        if (doc == null) {
            throw new IllegalStateException("No document with idx=" + i);
        }
        Number n = doc.get(COLUMN, Number.class);
        if (n == null) {
            throw new IllegalStateException("Field " + COLUMN + " missing at idx=" + i);
        }
        return n.doubleValue();
    }
}