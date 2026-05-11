const API_BASE = "/otp"

const identifierInput = document.getElementById("identifier")
const requestButton = document.getElementById("requestOtp")
const errorBox = document.getElementById("error")

requestButton.addEventListener("click", requestOtp)

async function requestOtp(){

    const identifier = identifierInput.value.trim()

    errorBox.innerText = ""

    /* Prevent empty request */

    if(identifier === ""){
        errorBox.innerText = "Please enter Email or Phone"
        return
    }

    /* Button loading state */

    requestButton.disabled = true
    requestButton.innerText = "Requesting OTP..."

    try{

        const response = await fetch(`${API_BASE}/request`,{
            method:"POST",
            headers:{
                "Content-Type":"application/json"
            },
            body: JSON.stringify({
                identifier: identifier
            })
        })

        const result = await response.json()

        if(result.status === "SUCCESS"){

            const requestId = result.data.requestId
            const expirySeconds = result.data.expirySeconds

            /* store values for verify page */

            localStorage.setItem("otp_identifier", identifier)
            localStorage.setItem("otp_requestId", requestId)
            localStorage.setItem("otp_expirySeconds", expirySeconds)

            /* redirect */

            window.location.href = "/verify.html"

        } else {

            errorBox.innerText = result.message || "OTP request failed"
            resetButton()

        }

    }catch(error){

        console.error(error)

        errorBox.innerText = "Server error. Try again."
        resetButton()

    }

}

function resetButton(){

    requestButton.disabled = false
    requestButton.innerText = "Request OTP"

}