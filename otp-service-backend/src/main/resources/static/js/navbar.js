document.addEventListener("DOMContentLoaded", () => {

const navbar = document.getElementById("navbar")
const footer = document.getElementById("footer")

if(navbar){
fetch("/components/navbar.html")
.then(res => res.text())
.then(data => navbar.innerHTML = data)
}

if(footer){
fetch("/components/footer.html")
.then(res => res.text())
.then(data => footer.innerHTML = data)
}

})