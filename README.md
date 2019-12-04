# Sample Application for the Android SDK

## Usage

###Clone this repository
````
git clone https://github.com/CyberSource/cybersource-android-samples
````

###Open the project in Android Studio and Run.

###Run through the sample mobile application and at the end you will get an encrypted payment blob which you can then pass back to your payment server without any PCI concerns.  This payment blob is then passed to a standard CyberSource payment authorization API request.

## Using the Payment Blob
Once you have the secure payment blob from our SDK you can post that up to your mobile backend/payment server (without incurring any additional PCI burden) and make a standard CyberSource API call.  For example, to authorize that card:  
  
````xml
<?xml version="1.0" encoding="UTF-8"?>
<soapenv:Envelope xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/">
  <soapenv:Header>
    <wsse:Security soapenv:mustUnderstand="1" xmlns:wsse="http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-secext-1.0.xsd">
      <wsse:UsernameToken>
        <wsse:Username>test_paymentech_001</wsse:Username>
        <wsse:Password Type="http://docs.oasis-open.org/wss/2004/01/oasis-200401-wssusername-token-profile-1.0#PasswordText">POgqicGXdrqafap/7WzT/KcYVzuN/aW4u1cuRKawBvOg==</wsse:Password>
      </wsse:UsernameToken>
    </wsse:Security>
  </soapenv:Header>
  <soapenv:Body>
    <nvpRequest xmlns="urn:schemas-cybersource-com:transaction-data-1.121">
ccAuthService_run=true
merchantID=test_paymentech_001
merchantReferenceCode=23AEE8CB6B62EE2AF07
encryptedPayment_data=eyJkYXRhIjoiN2kxTUxjWEIxRXZoMjk2emRJdnFyY1hZXC93SG9zRE9jbXdVRE1PRG5tdk9xWXJZWXlpTXhmRzc0bTRWbndTblwvN1FEdjlYUm9kZHNxalc4aXpoTVA4cXpDOG5HVkhMa3ZES2ZVYVNqWnB3WURKS2JaYk5JNklVcE1QUjlPWmJ2NFJ5VE5tbUprcmlVU25JbFwvbVRobWprbkh1eXpwc3FFRzdVR21wcXNkOHczaG50ZStvaUpSU2pBbmtraTI1T2hmUFlqTU9CUE9BaHNFMUsxM0Npc3dNWTA4dEN5K1V3VEhuakY5MThyTFVkXC9jMnpKTW9BOEFjN042SHFPQklWa2plUVwvd25rOGkyeGNcL3lubE51bTlMMlR5RGRnRE42SHNYWjNRb3V6Z053cTZyTGFSYjU5WGpMSlVXUmRDcU5namtFTjZlUkE0Mk94NElubDlQc0RKblRpbzZ4SDVcLyIsImhlYWRlciI6eyJhcHBsaWNhdGlvbkRhdGEiOiI0Nzc1Njk2NDNEMzczMDM4MzI2MzM5MzEzNjJENjI2MjY0NjMyRDM0MzY2NTY1MkQzODM0MzEzMDJENjM2NDM1MzEzNDMxMzQzNTMzNjUzNjM1M0I0NDYxNzQ2NTU0Njk2RDY1M0QzMjMwMzEzNTJEMzEzMjJEMzEzNDU0MzIzMDNBMzMzMTNBMzAzNTJFMzEzMjMzMzAzOTMwMzI1QSIsImVwaGVtZXJhbFB1YmxpY0tleSI6Ik1Ga3dFd1lIS29aSXpqMENBUVlJS29aSXpqMERBUWNEUWdBRU80V2prRzFLYXlpbmhlRGY5QXFtRVJCeHRxMHdiaFBmS25qcUNCZ0w4RWw4NU45c3JBWVdhaEE1d29iaHFTQ0VQazc5Q0xKNGhRVXU1bENIem43ZVdBPT0iLCJwdWJsaWNLZXlIYXNoIjoieXRLSjgwc3JjWXppQnVwNzRcL0R5K3RTV1FQSENwXC9ZbkFabGhcL3lXOFk3ND0iLCJ0cmFuc2FjdGlvbklkIjoiNDUwMTI1MDYyMDA3NTAwMDAwMTUxOSJ9LCJzaWduYXR1cmVBbGdJbmZvIjoiU0hBMjU2Iiwic2lnbmF0dXJlIjoiWitjbWg4UXNzNUdQMzUrOEEzTkZOcUNNRThFbVBqb0xiKzRqUWpIVDdGMD0iLCJ2ZXJzaW9uIjoiMS4xLjEuMiJ9
paymentSolution=004
billTo_firstName=Brian
billTo_lastName=McManus
billTo_city=Bellevue
billTo_country=US
billTo_email=bmc@mail.com
billTo_state=WA
billTo_street1=213 yiui St
billTo_postalCode=98103
purchaseTotals_currency=USD
purchaseTotals_grandTotalAmount=200.23
    </nvpRequest>
  </soapenv:Body>
</soapenv:Envelope>

````
