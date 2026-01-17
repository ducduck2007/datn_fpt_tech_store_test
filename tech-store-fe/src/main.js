import { createApp } from "vue";
import App from "./App.vue";
import router from "./router";

import { createPinia } from "pinia";

// Element Plus
import ElementPlus from "element-plus";
import "element-plus/dist/index.css";

// Keep Bootstrap CSS + icons for existing layout utility classes (NO bootstrap JS)
import "bootstrap/dist/css/bootstrap.min.css";
import "bootstrap-icons/font/bootstrap-icons.css";

import "./style.css";

const app = createApp(App);

app.use(createPinia());
app.use(router);
app.use(ElementPlus);

app.mount("#app");
