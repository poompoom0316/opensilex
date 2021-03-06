<template>
  <opensilex-Overlay :show="isLoaderVisible" :noFade="false" zIndex="32000">
    <div id="page-wrapper" class="wrapper customized" v-bind:class="{ embed: embed }">
      <component
        class="header-logo"
        v-bind:is="headerComponent"
        v-if="user.isLoggedIn() && !disconnected &&!embed"
      ></component>

      <header v-if="!embed" v-bind:class="{ 'logged-out': !user.isLoggedIn() || disconnected }">
        <component class="header-login" v-bind:is="loginComponent"></component>
      </header>

      <section id="content-wrapper" class="page-wrap" v-if="user.isLoggedIn() && !disconnected">
        <div>
          <component id="menu-container" v-if="!embed" v-bind:is="menuComponent"></component>
        </div>

        <div id="main-content">
          <main class="main-content">
            <router-view :key="$route.fullPath" />
          </main>

          <footer v-if="!embed">
            <component v-bind:is="footerComponent"></component>
          </footer>
        </div>
      </section>
    </div>
  </opensilex-Overlay>
</template>

<script lang="ts">
import { Component as ComponentAnnotation, Prop } from "vue-property-decorator";
import Vue from "vue";
import { ModuleComponentDefinition } from "./models/ModuleComponentDefinition";
import { VueConstructor, Component } from "vue";
import OpenSilexVuePlugin from "./models/OpenSilexVuePlugin";
import { FrontConfigDTO } from "./lib";

@ComponentAnnotation
export default class App extends Vue {
  @Prop() embed: boolean;

  @Prop() headerComponent!: string | Component;
  @Prop() loginComponent!: string | Component;
  @Prop() menuComponent!: string | Component;
  @Prop() footerComponent!: string | Component;

  $opensilex: OpenSilexVuePlugin;
  $bvToast: any;

  created() {
    this.$opensilex.$bvToast = this.$bvToast;
  }

  get disconnected() {
    return this.$store.state.disconnected;
  }

  get user() {
    return this.$store.state.user;
  }

  get isLoaderVisible() {
    return this.$store.state.loaderVisible;
  }
}
</script>

<style lang="scss">
@import "./styles/common.scss";

header {
  display: flex;
}

main {
  background-color: getVar(--defaultColorLight);
  color: getVar(--defaultColorDark);
}

#header-content {
  max-width: 1600px;
  margin: auto;
  display: flex;
  width: 100%;
}

#header-content .header-logo {
  width: 70%;
}

#header-content .header-login {
  width: 30%;
  text-align: right;
}

section#content-wrapper {
  display: flex;
  margin: 0 auto;
  height: 100%;
  flex-grow: 1;
  width: 100%;
}

main {
  padding: 15px;
  width: 100%;
}

.header-top.logged-out {
  box-shadow: none;
}

.wrapper.embed .page-wrap .main-content {
  margin-top: 0px;
  margin-left: 0px;
  padding: 15px;
}
</style>
