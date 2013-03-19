//bmi.js
// Set up vars to help with checking against the black list
var connection = new XMLHttpRequest();
// store our blacklist globally for convience
var blacklist = [];
var returnedData;


function processBMIform()
{
    var bmiFormObj = document.getElementById("bmiForm");
    if (validateInput(bmiFormObj))
    {
        var bmi = calculateBMI(bmiFormObj);
        if (bmiFormObj.details.checked)
            displayDetailed(bmiFormObj, bmi);
        else
            alert("Your BMI: " + precision(bmi));
    }
}

function validateInput(bmiFormObj)
{
    var hUnit = bmiFormObj.heightUnit.options[bmiFormObj.heightUnit.selectedIndex].text;
    var wUnit = bmiFormObj.weightUnit.options[bmiFormObj.weightUnit.selectedIndex].text;
    var height = bmiFormObj.height.value;
    var weight = bmiFormObj.weight.value;
    var email = bmiFormObj.email.value;
    var name = bmiFormObj.name.value;
    var heightOK, weightOK, emailOK, nameOK;

    if (hUnit == "inches")
        heightOK = validateInches(height);
    else
        heightOK = validateCentimetres(height);
    
    if (wUnit == "pounds")
        weightOK = validatePounds(weight);
    else
        weightOK = validateKilograms(weight);
    
    if (bmiFormObj.wantMail.checked)
    {
        emailOK = validateEmail(email);
		nameOK = validateName(name);
		if(emailOK && nameOK){
			alert("Warning: The e-mail feature is currently not supported.")
		}
    } else {
        emailOK = true;
		nameOK = true;
	}
  
    return heightOK && weightOK && emailOK && nameOK;
}

function validateInches(height)
{
    if (isNaN(height))
    {
        alert("Error: Please input a number for height.")
        return false;
    }
    
    if (height < 0 || height > 100)
    {
        alert("Error: Height must be in the range 0-100 inches.")
        return false;
    }
    
    return true;
}

function validateCentimetres(height)
{
    if (isNaN(height))
    {
        alert("Error: Please input a number for height.")
        return false;
    }
    
    if (height < 0 || height > 300)
    {
        alert("Error: Height must be in the range 0-300 centimeters.")
        return false;
    }
    
    return true;
}

function validatePounds(weight)
{
    if (isNaN(weight))
    {
        alert("Error: Please input a number for weight.")
        return false;
    }
    
    if (weight < 0 || weight > 1000)
    {
        alert("Error: Weight must be in the range 0-1000 pounds.")
        return false;
    }
    
    return true;
}

function validateKilograms(weight)
{
    if (isNaN(weight))
    {
        alert("Error: Please input a number for weight.")
        return false;
    }

    if (weight <= 0 || weight > 500)
    {
        alert("Error: Weight must be in the range 0-500 kilograms.")
        return false;
    }
    
    return true;
}

function validateEmail(address)
{
    var p = address.search(/.+@.+/);
    if (p == 0)
    {
        p = address.search(/.+ .+/);
        if (p != 0)
        {  
			// get our XML data
			getDynamicData();
			// loop through and check against the blacklist
			for(i = 0; i <= (blacklist.length -1); i++){
				if (address == blacklist[i]){
					alert("Error: blacklisted email address");
					return false;
				}
			}
            return true;
        }
        else
        {
            alert("Error: invalid e-mail address (contains whitespace).");
            return false;
        }
    }
    else
    {
        alert("Error: Invalid e-mail address (misses @).");
        return false;
    }
}

function validateName(name)
{
    var p = name.search(/.+ .+/);
    if (p == 0)
    {
        p = name.search(/^[A-Za-z ]+$/);
        if (p == 0)
        {  
            return true;
        }
        else
        {
            alert("Error: invalid Name (contains characters beyond alphabet [A-Z,a-z, ]).");
            return false;
        }

        return true;
    }
    else
    {
        alert("Error: invalid Name (has less than two parts).");
        return false;
    }
}

function calculateBMI(bmiFormObj)
{
    var hUnit = bmiFormObj.heightUnit.options[bmiFormObj.heightUnit.selectedIndex].text;
    var wUnit = bmiFormObj.weightUnit.options[bmiFormObj.weightUnit.selectedIndex].text;
    var height = bmiFormObj.height.value;
    var weight = bmiFormObj.weight.value;

    if (hUnit == "inches")
        height = inchesToCentimetres(height);
    
    if (wUnit == "pounds")
        weight = poundsToKilograms(weight);
    
    height /= 100; //Convert height from centimeters to meters
    var bmi = weight/(height*height); //kilograms/(meters*meters)
    return bmi;
}

function inchesToCentimetres(height)
{
    var CENTIMETRES_PER_INCH = 2.54;
    return height * CENTIMETRES_PER_INCH;
}

function poundsToKilograms(weight)
{
    var POUNDS_PER_KILOGRAM = 2.2;
    return weight / POUNDS_PER_KILOGRAM;
}

function precision(num)
{
    var intPortion = Math.floor(num);
    var decimalPortion = Math.round(num*10)%10;
    var text = intPortion + "." + decimalPortion;
    return text;
}

function displayDetailed(bmiFormObj, bmi)
{
    var hUnit = bmiFormObj.heightUnit.options[bmiFormObj.heightUnit.selectedIndex].text;
    var wUnit = bmiFormObj.weightUnit.options[bmiFormObj.weightUnit.selectedIndex].text;
    var height = bmiFormObj.height.value;
    var weight = bmiFormObj.weight.value;
    var text = "BMI Report\n" +
        "Your weight: " + weight + " " + wUnit + "\n" +
        "Your height: " + height + " " + hUnit + "\n" +
        "Your BMI: " + precision(bmi) + "\n";
    if (bmi < 18.5)
        text += "Your BMI suggests that you are underweight.\n";
    else if (bmi < 25)
        text += "Your BMI suggests that you have a reasonable weight.\n";
    else if (bmi < 29)
        text += "Your BMI suggests that you are overweight.\n";
    else
        text += "Your BMI suggests that you may be obese.\n";
    alert(text);
}

function getDynamicData(){
	// path to our dynamic data
	var path = "http://localhost:8080/BMI/emails.xml";
	// set up to send our request and handle it when it gets back, it will block until it gets data
	connection.open('GET', path, false);
	// send the request
	connection.send(null);
	// cast put the returned data into a variable
	returnedData = connection.responseXML.documentElement;
	// call our parser
	blacklistParse();

}

function blacklistParse(){
	// Array that we will use to pull out the addresses
	var emailsIndexed = returnedData.getElementsByTagName("email");
	// length of our array
	var array_length = emailsIndexed.length;
	// Loop to push into our blacklist array	
	for (i = 0; i < array_length; i++){
		blacklist.push(emailsIndexed[i].childNodes[0].nodeValue);
	}
}
