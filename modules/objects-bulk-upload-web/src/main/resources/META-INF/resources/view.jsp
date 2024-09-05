<%@ include file="init.jsp" %>

<portlet:resourceURL id="/download/object_template" var="downloadTemplate" />
<portlet:resourceURL id="/upload/object_template" var="uploadTemplate" />

<script src="https://cdnjs.cloudflare.com/ajax/libs/xlsx/0.18.5/xlsx.full.min.js"></script>

<div class="container mt-4">
    <div class="row">
        <div class="col-md-12 button-container">
            <h1 style="font-weight: bold;"><%= (String)portletPreferences.getValue("objectName", "") %></h1>

            <div class="buttons-wrapper">
                <button
                    id="downloadTemplateButton"
                    class="custom-button"
                    onClick="downloadTemplate()"
                >
                    <i class="fas fa-download"></i>
                </button>

                <button
                    class="custom-button"
                    id="uploadButton"
                    onclick="document.getElementById('data').click();"
                >
                    <i class="fas fa-upload"></i>
                </button>
                <input type="file" id="data" class="custom-file-input" accept=".xlsx"/>
            </div>
        </div>
    </div>

    <!-- Instructions container -->
    <%@ include file="instructions.jsp" %>

    <!-- error table container -->
    <div id="errorContainer" class="table-container"></div>

    <button id="startUploadButton" class="btn btn-outline-primary" style="display:none;" onClick="startBatch()">Start Upload</button>


    <!-- progress bar container -->
    <div id="batchProgressBarContainer" class="progress-bar-container" style="display:none;">
        <div class="progress progress-md">
            <div id="batchProgressBar" class="progress-bar" role="progressbar" style="width: 0%;">
                <span id="batchProgressBarLabel" class="progress-bar-label"></span>
            </div>
        </div>
        <div class="btn-upload-button" >
            <button id="startUploadButton" class="btn btn-outline-primary" onClick="startBatch()">Start Upload</button>
        </div>
    </div>
</div>

<script>
    var globalData, batchId;

     // Function to clear existing errors and reset the file input
     function clearExistingData() {
        $('#errorContainer').empty();
     }

     // reset button loader
    function resetButtonIcon(buttonId, iconClass) {
        const button = document.getElementById(buttonId);
        if (button) {
            // Clear existing content
            button.innerHTML = '';

            // Create a new icon element
            const iconElement = document.createElement('i');
            iconElement.className = iconClass; // Set the class of the icon

            // Append the icon element to the button
            button.appendChild(iconElement);

            button.disabled = false;
        } else {
            console.error('Button not found with ID:', buttonId);
        }
    }

     // show button loader
     function showLoader(buttonId) {
             const button = document.getElementById(buttonId);
             button.innerHTML = '<div class="spinner-border text-light" role="status"> <span class="sr-only">Loading...</span> </div>';
             button.disabled = true;
     }

    // Display selected file name next to the button
    document.getElementById("data").addEventListener("change", function () {
        clearExistingData();

        if (this.files.length === 0) {
                    return; // If no file is selected, do nothing
                }

        var fileName = this.files[0] ? this.files[0].name : "No file chosen";
        showLoader('uploadButton');

        var reader = new FileReader();
        reader.onload = (e) => {
            let data = e.target.result;
            let workbook = XLSX.read(data, { type: "binary" });
            let first_sheet_name = workbook.SheetNames[0];
            let worksheet = workbook.Sheets[first_sheet_name];
            var jsonObj = XLSX.utils.sheet_to_json(worksheet, { raw: false, defval: "" });

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
                        document.getElementById("data").value = "";
                        resetButtonIcon('uploadButton', 'fas fa-upload');
                        document.getElementById('instruction').style.display = 'none';

                    }else{
                        function showButtonAndBar() {
                            document.getElementById('batchProgressBarContainer').style.display = 'inline';
                        }

                        globalData = response.data;
                        showButtonAndBar();
                        document.getElementById("data").value = "";
                        resetButtonIcon('uploadButton', 'fas fa-upload');
                        document.getElementById('instruction').style.display = 'none';
                    }
                }
            });
        };

        reader.readAsArrayBuffer(this.files[0]);
    });

    // On button click - download template
    function downloadTemplate() {
        showLoader('downloadTemplateButton');

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
                resetButtonIcon('downloadTemplateButton', 'fas fa-download');
            },
            error: function () {
                alert("An error occurred while generating the Excel file.");
                $("#downloadTemplateButton").text("Failed");

                // Revert button text
                resetButtonIcon('downloadTemplateButton', 'fas fa-download');
            },
        });
    }

     // On button click - upload template
     function startBatch() {
        Liferay.Util.fetch('<%=(String)portletPreferences.getValue("restContextPath", "")%>/batch', {
           body: JSON.stringify(globalData),
           headers: { "Content-Type": "application/json"},
           method: 'POST'
        }).then(function(response) {
          return response.json();
        }).then(function(response) {
          console.log(response);

          var batchId = response.id;

          // If returnStatus is INITIAL, start checking the status every 5 seconds
          if (response.executeStatus === "INITIAL") {
            var intervalId = setInterval(function() {
                checkBatchStatus(batchId, intervalId);
            }, 2000);
          }else{
            $("#startUploadButton").text("Failed");
          }

        }).catch(function() {
        });

     }

     // Check batch status every 2 seconds
     function checkBatchStatus(batchId, intervalId) {
         Liferay.Util.fetch('/o/headless-batch-engine/v1.0/import-task/' + batchId, {
             headers: {
                 "Content-Type": "application/json"
             },
             method: 'GET'
         }).then(function(response) {
             return response.json();
         }).then(function(response) {
             console.log(response);

             // Get the total and processed items count
             var totalItemsCount = response.totalItemsCount;
             var processedItemsCount = response.processedItemsCount;

             // Calculate progress percentage
             var progressPercentage = (processedItemsCount / totalItemsCount) * 100;

             // Update the progress bar width and label
             var progressBar = document.getElementById('batchProgressBar');
             var progressBarLabel = document.getElementById('batchProgressBarLabel');

             progressBar.style.width = progressPercentage + '%';
             progressBarLabel.innerText = `Progress: ${processedItemsCount} / ${totalItemsCount}`;

             // If the status is COMPLETED, stop checking
             if (response.executeStatus === "COMPLETED") {
                 clearInterval(intervalId);
                 console.log('Batch processing completed.');
                 progressBarLabel.innerText = `Completed`;
             }else if(response.executeStatus === "FAILED"){
                clearInterval(intervalId);
                console.log('Batch processing failed.');
                progressBarLabel.innerText = `Failed`;
             }
         }).catch(function() {
             // Handle error
             console.error('Error fetching batch status');
         });
     }
</script>