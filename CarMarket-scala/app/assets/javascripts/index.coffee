$ ->
  $.get "/cars", (cars) ->
    $.each cars, (index, cars) ->
      name = $("<div>").addClass("name").text car.name
      color = $("<div>").addClass("color").text car.color
      $("#colors").append $("<li>").append(name).append(color)