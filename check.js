const https = require('https');
const data = JSON.stringify({
  contents: [{ parts: [{ text: "What is the weather in Tokyo?" }] }],
  tools: [{ googleSearchRetrieval: { dynamicRetrievalConfig: { mode: "MODE_DYNAMIC", dynamicThreshold: 0.3 } } }]
});
console.log("Using googleSearchRetrieval:", data);

const data2 = JSON.stringify({
  contents: [{ parts: [{ text: "What is the weather in Tokyo?" }] }],
  tools: [{ googleSearch: {} }]
});
console.log("Using googleSearch:", data2);
