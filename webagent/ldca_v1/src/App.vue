<template>
  <div>
    <Dialog
      request="Login with Profile URI:"
      :showDialog="requireLogin"
      @returnValue="login"
    />
    <md-app md-mode="fixed">
      <!-- <div id="nav">
        <router-link to="/">Home</router-link> |
        <router-link to="/about">About</router-link>
      </div>-->
      <md-app-toolbar class="md-primary center">
        <h3 class="md-title" style="flex: 1">Linked Data Chain Agent</h3>
        <h3 class="md-title">v0.1.0</h3>
      </md-app-toolbar>

      <!-- Messenger view-->

      <md-app-drawer v-if="profile.uri" md-permanent="full">
        <Inbox :profile="profile" :deFocus="!focusItem" @focusItem="focusOn" />
      </md-app-drawer>
      <md-app-content v-if="profile.uri" id="content">
        <!-- <router-view /> -->
        <div v-if="!focusItem && !selectedCreator">
          <md-card>
            <md-card-content>
              <md-empty-state
                class="md-primary"
                md-rounded
                md-icon="access_time"
                md-label="That's nothin' to Snooze at!"
                md-description="Select an Inbox Item or click the Speed Dial. More information will be displayed here."
              />
            </md-card-content>
          </md-card>
          <md-speed-dial class="md-bottom-right">
            <md-tooltip md-direction="left">
              Create a new Linked Pedigree!
            </md-tooltip>
            <md-speed-dial-target class="md-primary" @click="create">
              <md-icon class="md-morph-initial">add</md-icon>
              <md-icon class="md-morph-final">edit</md-icon>
            </md-speed-dial-target>
          </md-speed-dial>
        </div>
        <div v-if="focusItem">
          <!-- Linked Pedigree view-->
          <LinkedPedigree
            :profile="profile"
            :item="focusItem"
            @deletedURI="backToSnooze"
          />
        </div>
        <div v-if="selectedCreator">
          <PedigreeCreator :profile="profile" />
        </div>
        <div v-if="focusItem || selectedCreator">
          <md-speed-dial class="md-bottom-right">
            <md-tooltip md-direction="left">
              Back to snooze!
            </md-tooltip>
            <md-speed-dial-target class="md-accent" @click="backToSnooze">
              <md-icon class="md-morph-initial">pause</md-icon>
              <md-icon class="md-morph-final">snooze</md-icon>
            </md-speed-dial-target>
          </md-speed-dial>
        </div>
      </md-app-content>
    </md-app>
  </div>
</template>

<script>
// @ is an alias to /src
import Dialog from "@/components/Dialog.vue";
import Inbox from "@/views/Inbox.vue";
import LinkedPedigree from "@/views/LinkedPedigree.vue";
import PedigreeCreator from "@/views/PedigreeCreator.vue";
import { getN3 } from "@/lib/n3Requests.js";
import { getProfileDataFromN3 } from "@/lib/n3Query.js";
export default {
  name: "App",
  components: {
    Dialog,
    Inbox,
    LinkedPedigree,
    PedigreeCreator
  },
  props: {},
  data() {
    return {
      requireLogin: true,
      profile: {
        uri: null,
        name: null,
        depiction: null,
        inbox: null,
        outbox: null,
        docbox: null,
        wallet: null,
        ledger: null,
        hasher: null
      },
      focusItem: null,
      selectedCreator: null
    };
  },
  methods: {
    async login(profileURI) {
      console.log("### App\t\t| Received login\n" + profileURI);
      if (!profileURI.startsWith("http://")) {
        // default shortcut
        profileURI = "http://aifb-ls3-vm4.aifb.kit.edu:9999/rdf/profile-".concat(
          profileURI
        );
      }
      this.profile = await getN3(profileURI).then(n3 =>
        getProfileDataFromN3(n3.store, n3.baseIRI)
      );
      console.log("### App\t\t| Logged in by\n" + this.profile.uri);
      this.requireLogin = false;
    },
    focusOn(focusItem) {
      console.log("### App\t\t| Focus on\n" + focusItem.uri);
      this.selectedCreator = false;
      this.focusItem = focusItem;
    },
    backToSnooze() {
      this.focusItem = null;
      this.selectedCreator = false;
    },
    create() {
      console.log("### App\t\t| Create new LP\n");
      this.focusItem = null;
      this.selectedCreator = true;
    }
  }
};
</script>

<style lang="scss">
#content {
  background-color: whitesmoke;
  min-height: 100%;
  height: auto;
}

.spacer {
  flex: 1;
}

.center {
  text-align: center;
}

.md-app {
  -webkit-font-smoothing: antialiased;
  -moz-osx-font-smoothing: grayscale;
  border: none;
  height: 100vh;
}

.md-app-drawer {
  width: 25%;
}

/* width */
::-webkit-scrollbar {
  display: none; // but when I do, it looks sweet.
  background: #c5daee;
  height: 200px;
  width: 5px;
}

/* Track */
::-webkit-scrollbar-track {
  border: none;
}

/* Handle */
::-webkit-scrollbar-thumb {
  background: #3d85c6;
}

/* Handle on hover */
::-webkit-scrollbar-thumb:hover {
  background: #1a55a4;
}
</style>
