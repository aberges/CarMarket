$ ->
  $.get "/cars", (cars) ->
    $.each cars, (index, car) ->
      id = $("<div>").addClass("id").text car.id
      name = $("<div>").addClass("name").text car.name
      color = $("<div>").addClass("color").text car.color
      $("#cars").append $("<li>").append(id).append(name).append(color)