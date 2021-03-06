<template>
  <opensilex-WizardForm
    ref="wizardRef"
    :steps="steps"
    createTitle="ExperimentForm.create"
    editTitle="ExperimentForm.update"
    icon="fa#vials"
    modalSize="lg"
    :initForm="getEmptyForm"
    :createAction="create"
    :updateAction="update"
  >
    <template v-slot:icon></template>
  </opensilex-WizardForm>
</template>

<script lang="ts">
import { Component, Prop, PropSync, Ref } from "vue-property-decorator";
import Vue from "vue";
import VueRouter from "vue-router";

import {
  ExperimentCreationDTO,
  SpeciesService,
  SpeciesDTO
} from "opensilex-core/index";
import HttpResponse, { OpenSilexResponse } from "opensilex-core/HttpResponse";

@Component
export default class ExperimentForm extends Vue {
  $opensilex: any;
  $t: any;

  @Ref("wizardRef") readonly wizardRef!: any;

  steps = [
    {
      component: "opensilex-ExperimentForm1"
    },
    {
      component: "opensilex-ExperimentForm2"
    }
  ];

  getEmptyForm() {
    return {
      uri: undefined,
      label: "",
      projects: [],
      startDate: undefined,
      endDate: undefined,
      objective: "",
      comment: "",
      keywords: [],
      scientificSupervisors: [],
      technicalSupervisors: [],
      groups: [],
      infrastructures: [],
      installations: [],
      species: [],
      isPublic: false,
      variables: [],
      sensors: [],
      factors: []
    };
  }

  showCreateForm() {
    this.wizardRef.showCreateForm();
  }

  showEditForm(form) {
    this.wizardRef.showEditForm(form);
  }

  create(form) {
    this.$opensilex
      .getService("opensilex.ExperimentsService")
      .createExperiment(form)
      .then((http: HttpResponse<OpenSilexResponse<any>>) => {
        let uri = http.response.result;
        form.uri = uri;
        console.debug("experiment created", uri);
        this.$emit("onCreate", form);
      })
      .catch(error => {
        if (error.status == 409) {
          console.error("Experiment already exists", error);
          this.$opensilex.errorHandler(
            error,
            this.$t("ExperimentForm.experiment-already-exists")
          );
        } else {
          this.$opensilex.errorHandler(error);
        }
      });
  }

  update(form) {
    this.$opensilex
      .getService("opensilex.ExperimentsService")
      .updateExperiment(form)
      .then((http: HttpResponse<OpenSilexResponse<any>>) => {
        let uri = http.response.result;
        console.debug("experiment updated", uri);
        this.$emit("onUpdate", form);
      })
      .catch(this.$opensilex.errorHandler);
  }
}
</script>

<style scoped lang="scss">
</style>

<i18n>
en:
  ExperimentForm:
    create: Create experiment
    update: Update experiment
    experiment-already-exists: Experiment already exists

fr:
  ExperimentForm:
    create: Créer une expérimentation
    update: Modifier l'expérimentation
    experiment-already-exists: L'expérimentation existe déjà
</i18n>