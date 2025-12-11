document.getElementById("searchGameAccount").addEventListener("input", function () {
    const keyword = this.value.toLowerCase().trim();
    const rows = document.querySelectorAll("#myTable tbody tr");

    rows.forEach(row => {
        const gameAcc = row.children[1].innerText.toLowerCase();
        row.style.display = gameAcc.includes(keyword) ? "" : "none";
    });
});