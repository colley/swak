
const request = axios.create();
request.defaults.timeout = 120000;

request.interceptors.request.use(
	config => {
		config.url = "http://192.168.10.107:8081" + config.url;
		return config
	},
	error => {
		return Promise.reject(error.message)
	}
)



request.interceptors.response.use(function (response) {
	let data = response.data;
	if (data.success) {
		return data.obj;
	} else {
		//alert(data.msg);
	}

}, function (error) {
		//alert('服务器繁忙，请稍后再试！');
	return Promise.reject(error);
});


