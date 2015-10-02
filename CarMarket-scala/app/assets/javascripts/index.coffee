$ ->
  $.get "/cars", (cars) ->
    $.each cars, (index, car) ->
      id = $("<div>").addClass("id").text car.id
      title = $("<div>").addClass("name").text car.title
      fuel = $("<div>").addClass("color").text car.fuel
      price = $("<div>").addClass("color").text car.price
      isNew = $("<div>").addClass("color").text car.isNew
      mileAge = $("<div>").addClass("color").text car.mileAge
      firstRegistration = $("<div>").addClass("color").text new Date(car.firstRegistration)
      $("#cars").append $("<li>").append(id).append(title).append(fuel).append(price).append(isNew).append(mileAge).append(firstRegistration)