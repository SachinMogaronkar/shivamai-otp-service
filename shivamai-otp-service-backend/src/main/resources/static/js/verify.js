const API="/otp"

const otpInputs=document.querySelectorAll(".otp")

function getOtp(){

let otp=""

otpInputs.forEach(i=>otp+=i.value)

return otp

}

document.getElementById("verifyBtn").onclick=async()=>{

const requestId=localStorage.getItem("requestId")

const otp=getOtp()

const res=await fetch(`${API}/verify`,{

method:"POST",
headers:{"Content-Type":"application/json"},

body:JSON.stringify({

requestId:requestId,
otp:otp

})

})

const result=await res.json()

if(result.status==="SUCCESS"){

document.getElementById("message").innerText="✔ Verification Successful"

}

}