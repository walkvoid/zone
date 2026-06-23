$(function () {
	"use strict";

	/* Bar-Chart1 */
	var ctx = document.getElementById("barChart");
	var myChart = new Chart(ctx, {
		type: 'bar',
		data: {
			labels: ["Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul"],
			datasets: [{
				label: "Sales",
				data: [65, 59, 80, 81, 56, 55, 40],
				borderColor: "#6610f2",
				borderWidth: "0",
				backgroundColor: "#6610f2"
			}, {
				label: "Orders",
				data: [28, 48, 40, 19, 86, 27, 90],
				borderColor: "#D81E5B",
				borderWidth: "0",
				backgroundColor: "#D81E5B"
			}]
		},
		options: {
			responsive: true,
			maintainAspectRatio: false,
			scales: {
				x: {
					ticks: {
						fontColor: "#8492a6",
					 },
					gridLines: {
						color: 'rgba(119, 119, 142, 0.2)'
					}
				},
				y: {
					ticks: {
						beginAtZero: true,
						fontColor: "#8492a6",
					},
					gridLines: {
						color: 'rgba(119, 119, 142, 0.2)'
					},
				}
			},
			legend: {
				labels: {
					fontColor: "#8492a6"
				},
			},
		}
	});

	/* polar chart */
	var ctx = document.getElementById("polarChart");
	var myChart = new Chart(ctx, {
		type: 'polarArea',
		data: {
			datasets: [{
				data: [18, 15, 9, 6, 19],
				backgroundColor: ['#6610f2', '#53caed', '#01b8ff', '#f16d75', '#29ccbb'],
				hoverBackgroundColor: ['#6610f2', '#53caed', '#01b8ff', '#f16d75', '#29ccbb'],
				borderColor:'transparent',
			}],
			labels: ["Completed", "In Progress", "Yet To Start", "Cancelled"]
		},
		options: {
			scale: {
				gridLines: {
						color: 'rgba(119, 119, 142, 0.2)'
				}
			},
			responsive: true,
			maintainAspectRatio: false,
			legend: {
				labels: {
					fontColor: "#8492a6"
				},
			},
		}
	});

	/* Pie Chart*/
	var datapie = {
		labels: ['Direct', 'Social', 'Email', 'Other', 'Referral'],
		datasets: [{
			data: [15, 20, 20, 10, 20],
			backgroundColor: ['#6610f2', '#D81E5B', '#01b8ff', '#f16d75', '#29ccbb']
		}]
	};
	var optionpie = {
		maintainAspectRatio: false,
		responsive: true,
		legend: {
			display: false,
		},
		animation: {
			animateScale: true,
			animateRotate: true
		}
	};
	/* Pie Chart*/
	var ctx7 = document.getElementById('pieChart');
	var myPieChart7 = new Chart(ctx7, {
		type: 'pie',
		data: datapie,
		options: datapie
	});
});