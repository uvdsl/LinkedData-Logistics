<template>
  <md-dialog
    :md-active.sync="showDialog"
    :md-close-on-esc="optionalDialog"
    :md-click-outside-to-close="optionalDialog"
  >
    <md-dialog-title>{{ request }}</md-dialog-title>
    <md-dialog-content>
      <md-field md-inline>
        <md-icon>person</md-icon>
        <md-input v-model="value" @keyup.enter="submit()"></md-input>
      </md-field>
    </md-dialog-content>

    <md-dialog-actions>
      <md-button class="md-primary" @click="submit()">
        Go!
      </md-button>
    </md-dialog-actions>
  </md-dialog>
</template>

<script>
export default {
  name: "Dialog",
  props: { request: String, showDialog: Boolean },
  data: () => ({
    optionalDialog: false,
    value: "trucker"
  }),
  methods: {
    submit() {
      console.log("### Dialog\t| Submits\n" + this.value);
      this.$emit("returnValue", this.value);
      this.value = "trucker";
    }
  }
};
</script>
