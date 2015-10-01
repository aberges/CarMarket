$ ->
  $.get "/cars", (cars) ->
    $.each cars, (index, car) ->
      name = $("<div>").addClass("name").text car.name
      color = $("<div>").addClass("color").text car.color
      $("#cars").append $("<li>").append(name).append(color)