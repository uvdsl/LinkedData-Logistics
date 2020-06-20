import "@/polyfills-window-setImmediate";
import Vue from "vue";
import {
  MdApp,
  MdAvatar,
  MdButton,
  MdCard,
  MdCheckbox,
  MdContent,
  MdDialog,
  MdDialogPrompt,
  MdDivider,
  MdDrawer,
  MdEmptyState,
  MdField,
  MdIcon,
  MdList,
  MdMenu,
  MdProgress,
  MdRipple,
  MdSpeedDial,
  MdToolbar,
  MdTooltip
} from "vue-material/dist/components";
import "vue-material/dist/vue-material.min.css";
import "vue-material/dist/theme/default.css";
import App from "./App.vue";
import router from "./router";
import store from "./store";

Vue.config.productionTip = false;
Vue.use(MdApp);
Vue.use(MdAvatar);
Vue.use(MdButton);
Vue.use(MdCard);
Vue.use(MdCheckbox);
Vue.use(MdContent);
Vue.use(MdDialog);
Vue.use(MdDialogPrompt);
Vue.use(MdDivider);
Vue.use(MdDrawer);
Vue.use(MdEmptyState);
Vue.use(MdField);
Vue.use(MdIcon);
Vue.use(MdList);
Vue.use(MdMenu);
Vue.use(MdProgress);
Vue.use(MdRipple);
Vue.use(MdSpeedDial);
Vue.use(MdToolbar);
Vue.use(MdTooltip);

new Vue({
  router,
  store,
  render: h => h(App)
}).$mount("#app");
