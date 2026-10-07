library(lattice)

data <<- numeric(100)

function(dataHolder) {
  svg()
  data <<- c(data[2:100], dataHolder$value)

  plot <- xyplot(
    data ~ time,
    data = data.frame(data = data, time = 0:99),
    main = "SWE 307 - Col-1 Live Data",
    xlab = "Time (0-99)",
    ylab = "Value",
    ylim = c(-1.1, 1.1),
    type = c("l", "g"),
    col.line = "#654321",
    lwd = 2
  )

  print(plot)
  svg.off()
}