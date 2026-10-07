# SWE 307 – Big Data Project 1: Data Visualization using R

A Java Spring Boot web application that reads **one value per second** from MongoDB,
passes it to an **R** plotting function running on **GraalVM (FastR)**, and shows the
live chart in the browser. Our group uses **Col-1**.

## How it works

Browser --GET /plot--> PlotController --next()--> DataService --findOne--> MongoDB
                            |
                            v
                  R function (plot.R) via GraalVM --> SVG --> Browser (Refresh: 1)

- `DataService` reads one row (`Row-1` ... `Row-100`) from MongoDB per request.
- `PlotController` sends that value to R and returns the SVG with a `Refresh: 1` header.
- `plot.R` keeps the last 100 values and draws them with lattice `xyplot()`:
  line type, dark brown color, grid, x-axis 0-99.

## Requirements

- GraalVM CE 22.3.0 (Java 17) with the R component (`gu install R`)
- MongoDB running on `localhost:27017`
- Maven

## Import the data

    sed '1s/^,/Row,/' swe307_pro1.csv > pro1.csv
    mongoimport --db swe307 --collection pro1 --type csv --headerline --drop --file pro1.csv

## Run

    mvn spring-boot:run

Open http://localhost:8080/plot
