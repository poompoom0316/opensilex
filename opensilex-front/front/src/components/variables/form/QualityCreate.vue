<template>
    <opensilex-WizardForm
            ref="wizardRef"
            :steps="steps"
            createTitle="component.variable.form.add.quality-description"
            editTitle="component.variable.form.add.quality-description"
            icon="fa#vials"
            modalSize="lg"
            :initForm="getEmptyForm"
            :createAction="create"
            :updateAction="update"
            :static="false"
    >
        <template v-slot:icon></template>
    </opensilex-WizardForm>
</template>


<script lang="ts">
    import {Component, Prop, Ref} from "vue-property-decorator";
    import Vue from "vue";
    import HttpResponse, {OpenSilexResponse} from "opensilex-security/HttpResponse";
    import {VariablesService} from "opensilex-core/api/variables.service";
    import {QualityCreationDTO} from "opensilex-core/model/qualityCreationDTO";

    @Component
    export default class QualityCreate extends Vue {

        steps = [
            {component: "opensilex-QualityForm"}
        ];

        title = "";
        uriGenerated = true;
        editMode = false;
        errorMsg: String = "";
        service: VariablesService;

        @Ref("wizardRef") readonly wizardRef!: any;

        created(){
            this.service = this.$opensilex.getService("opensilex.VariablesService");
        }

        handleErrorMessage(errorMsg: string) {
            this.errorMsg = errorMsg;
        }

        showCreateForm() {
            this.wizardRef.showCreateForm();
        }

        showEditForm() {
            this.wizardRef.showEditForm();
        }

        $opensilex: any;

        @Ref("modalRef") readonly modalRef!: any;
        @Ref("validatorRef") readonly validatorRef!: any;

        getEmptyForm(): QualityCreationDTO {
            return {
                uri: null,
                label: null,
                comment: null,
                type: null,
                exactMatch: [],
                narrower: [],
                closeMatch: [],
                broader: []
            };
        }

        create(form){
            if(form.type){
                form.type = form.type.uri;
            }
            return this.service
                .createQuality(form)
                .then((http: HttpResponse<OpenSilexResponse<any>>) => {
                    form.uri = http.response.result;
                    this.$emit("onCreate", form);
                })
                .catch(error => {
                    if (error.status == 409) {
                        console.error("Quality already exists", error);
                        this.$opensilex.errorHandler(
                            error,
                            this.$i18n.t("component.project.errors.project-already-exists")
                        );
                    } else {
                        this.$opensilex.errorHandler(error);
                    }
                });
        }

        update(form){

        }

        loadingWizard: boolean = false;

        setLoading(value: boolean) {
            this.loadingWizard = value;
        }

    }

</script>

<style scoped lang="scss">
</style>