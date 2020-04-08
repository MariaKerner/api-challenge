# api-challenge
Safe Harbor De-Identification

To Build/Run the API:  

  generate a .war file with "mvn package", run under Tomcat (I ran with Tomcat 9.0.33)



Problem Statement
Write an API that exposes an endpoint for the de-identification of patient records. Specifically, we're looking for the following:

Birthdates should be converted to the patient's age. If someone is over the age of 89, they should be lumped into a 90+ category.

ZIP Codes should be stripped to the first three digits except where fewer than 20,000 people reside in the combination of all ZIP codes with those three digits. In this case, the ZIP Code should be set to 00000. A file with ZIP codes and their populations is included in CSV format. Note that these are Zip Code Tabulation Areas (ZCTAs) which exclude certain ZIP codes which are not useful for population data (i.e. some office buildings have their own ZIP codes due to mail volume, but are not considered for census tabulation).

Admission and Discharge dates should be set to the year only.

The notes section should replace anything that looks like an email address, US social security number, or a US telephone number with sensible replacements. Any dates in the notes section should be replaced with the year.

You may assume that names, pictures, etc. will not occur in the data. You can assume data is reasonably well formed (i.e. no one will try to pass a date as a zip code) but the server should ideally not crash if it encounters an edge case.

Sample Inputs and Outputs
A sample input of:

{
    "birthDate": "2000-01-01",
    "zipCode": "10013",
    "admissionDate": "2019-03-12",
    "dischargeDate": "2019-03-14",
    "notes": "Patient with ssn 123-45-6789 previously presented under different ssn"
}
Should yield output of:

{
    "age": "20",
    "zipCode": "10000",
    "admissionYear": "2019",
    "dischargeYear": "2019",
    "notes": "Patient with ssn XXX-XX-XXXX previously presented under different ssn"
}
