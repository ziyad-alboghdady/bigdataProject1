library(lattice)

# Initialize buffer with 100 zeros
data <<- numeric(100)

function(dataHolder) {
  svg()
  # Shift window left and append new incoming value
  data <<- c(data[2:100], dataHolder$value)

  # Plot line on grid: x from 0 to 99, line color dark brown
  plot <- xyplot(
    data ~ time,
    data = data.frame(data = data, time = 0:99),
    main = "Big Data Measurement Plot",
    xlab = "Time (0-99)",
    ylab = "Value",
    type = c("l", "g"),
    col.line = "saddlebrown"
  )

  print(plot)
  svg.off()
}