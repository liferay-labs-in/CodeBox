<%@ include file="init.jsp" %>

<!-- Include the Bootstrap CSS -->
<link rel="stylesheet" href="https://stackpath.bootstrapcdn.com/bootstrap/4.5.2/css/bootstrap.min.css">

<!-- Custom CSS -->
<style>
    .button-container {
        display: flex;
        align-items: center;
        margin-top: 10px;
        gap: 10px; /* Space between buttons and file name */
    }

    .custom-file-input-wrapper {
        position: relative;
        overflow: hidden;
        display: inline-block;
    }

    .custom-file-input-wrapper input[type="file"] {
        position: absolute;
        left: 0;
        top: 0;
        right: 0;
        bottom: 0;
        opacity: 0;
        cursor: pointer;
        width: 100%;
    }

    .custom-file-input-wrapper .btn-file {
        background-color: #007bff;
        color: white;
        padding: 8px 20px;
        border-radius: 5px;
        border: none;
        cursor: pointer;
        display: inline-block;
    }

    .custom-file-input-wrapper .btn-file:hover {
        background-color: #0056b3;
    }

    #fileNameDisplay {
        color: #6c757d;
        margin-left: 10px; /* Space between file name and file input */
        vertical-align: middle;
    }

    .table-container {
        margin-top: 20px;
    }

    .error-table {
        width: 100%;
        margin-top: 20px;
    }
</style>

<portlet:resourceURL id="/download/object_template" var="downloadTemplate" />
<portlet:resourceURL id="/upload/object_template" var="uploadTemplate" />

<script src="https://cdnjs.cloudflare.com/ajax/libs/xlsx/0.18.5/xlsx.full.min.js"></script>

<div class="container mt-4">
    <div class="row">
        <div class="col-md-9 button-container">
            <button
                id="downloadTemplateButton"
                class="btn btn-primary"
                onClick="downloadTemplate()"
            >
                Download Template
            </button>

            <!-- Custom-styled file input -->
            <div class="custom-file-input-wrapper">
                <button class="btn-file">Choose File</button>
                <input type="file" id="data" accept=".xlsx" />
            </div>

            <span id="fileNameDisplay">No file chosen</span>
        </div>
    </div>

    <div id="errorContainer" class="table-container"></div>

    <button id="startUploadButton" style="display:none;" onClick="startBatch()">Start Upload</button>
</div>

<script>

    var globalData;

    // Display selected file name next to the button
    document.getElementById("data").addEventListener("change", function () {
        var fileName = this.files[0] ? this.files[0].name : "No file chosen";
        document.getElementById("fileNameDisplay").textContent = "Validating...";


        var reader = new FileReader();
        reader.onload = (e) => {
            let data = e.target.result;
            let workbook = XLSX.read(data, { type: "binary" });
            let first_sheet_name = workbook.SheetNames[0];
            let worksheet = workbook.Sheets[first_sheet_name];
            var jsonObj = XLSX.utils.sheet_to_json(worksheet, { raw: false });
            console.log(jsonObj);

            $.ajax({
                url: '<%= uploadTemplate %>',
                datatype: 'json',
                type: 'POST',
                data: {
                    <portlet:namespace/>data: JSON.stringify(jsonObj),
                },
                success: function (response) {
                    console.log(response);

                    if (response.status === "Failed") {
                        // Create a table element with Bootstrap classes
                        var table = $('<table class="table error-table"></table>');

                        // Create a table header row
                        var headerRow = $('<tr></tr>');
                        headerRow.append($('<th>Cell</th>'));
                        headerRow.append($('<th>Error</th>'));
                        table.append(headerRow);

                        // Function to convert column number to Excel-style column label
                        function getColumnLabel(columnNumber) {
                            let columnLabel = '';
                            while (columnNumber > 0) {
                                let remainder = (columnNumber - 1) % 26;
                                columnLabel = String.fromCharCode(65 + remainder) + columnLabel;
                                columnNumber = Math.floor((columnNumber - 1) / 26);
                            }
                            return columnLabel;
                        }

                        // Loop through each error in the response data and create a row for each error
                        response.data.forEach(function (error) {
                            // Get Excel-style column label
                            var columnLabel = getColumnLabel(error.column);
                            // Create cell reference (e.g., "A1")
                            var cellReference = columnLabel + error.row;

                            var row = $('<tr></tr>');
                            row.append($('<td></td>').text(cellReference));
                            row.append($('<td></td>').text(error.error));
                            table.append(row);
                        });

                        // Append the table to a specific container in your HTML
                        $('#errorContainer').html(table);
                    }else{
                        function showButton() {
                            document.getElementById('startUploadButton').style.display = 'inline';
                        }

                        globalData = response.data;
                        showButton();
                    }

                    document.getElementById("fileNameDisplay").textContent = fileName;
                }
            });
        };
        reader.readAsArrayBuffer(this.files[0]);
    });

    // On button click - download template
    function downloadTemplate() {
        var button = $("#downloadTemplateButton").text("Loading...");
        $.ajax({
            url: "<%=downloadTemplate%>",
            method: "GET",
            xhrFields: {
                responseType: "blob", // Important to specify this for binary data
            },
            success: function (data) {
                // Create a Blob from the data
                var blob = new Blob([data], {
                    type: "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                });
                var url = window.URL.createObjectURL(blob);

                // Create a link element, use it to download the Blob, and remove it after
                var a = document.createElement("a");
                a.href = url;
                a.download = "template.xlsx"; // This will be the filename
                document.body.appendChild(a);
                a.click();
                a.remove();

                // Revert button text
                $("#downloadTemplateButton").text("Download Template");
            },
            error: function () {
                alert("An error occurred while generating the Excel file.");
                $("#downloadTemplateButton").text("Failed");
            },
        });
    }

     // On button click - upload template
     function startBatch() {

        Liferay.Util.fetch('/o/c/<%=(String)portletPreferences.getValue("objectLabel", "")%>/batch', {
          body: globalData,
          headers: {
          	   		 "Content-Type": "application/json; charset=utf-8"
          			},
          method: 'POST'
        }).then(function(response) {
          return response.json();
        }).then(function(response) {
          console.log(response);
        }).catch(function() {
        });

         $.ajax({
            url: ,
            type: 'POST',
            data: JSON.stringify(globalData),
            contentType: "application/json; charset=utf-8",
            traditional: true,
            success: function (response) {
                console.log(response);
            }
         });
     }
</script>
