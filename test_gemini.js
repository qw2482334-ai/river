const https = require('https');
const data = JSON.stringify({
  contents: [{ parts: [{ text: "What is the weather in Tokyo today?" }] }],
  tools: [{ googleSearch: {} }]
});
console.log(data);
