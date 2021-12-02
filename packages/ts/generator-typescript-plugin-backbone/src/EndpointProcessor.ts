import createSourceFile from '@vaadin/generator-typescript-utils/createSourceFile.js';
import DependencyManager from '@vaadin/generator-typescript-utils/DependencyManager.js';
import PathProcessor from '@vaadin/generator-typescript-utils/PathProcessor.js';
import { OpenAPIV3 } from 'openapi-types';
import type { ReadonlyDeep } from 'type-fest';
import type { SourceFile, Statement } from 'typescript';
import EndpointMethodOperationProcessor, { EndpointMethodOperation } from './EndpointMethodOperationProcessor.js';
import type { BackbonePluginContext } from './utils.js';
import { clientLib } from './utils.js';

export default class EndpointProcessor {
  readonly #context: BackbonePluginContext;
  readonly #dependencies = new DependencyManager(new PathProcessor('.'));
  readonly #methods = new Map<string, ReadonlyDeep<OpenAPIV3.PathItemObject>>();
  readonly #name: string;
  readonly #sourcePathProcessor = new PathProcessor('.', 'ts');

  public constructor(name: string, context: BackbonePluginContext) {
    this.#context = context;
    this.#name = name;
    this.#dependencies.imports.default.add(clientLib.path, clientLib.specifier);
  }

  public add(method: string, pathItem: ReadonlyDeep<OpenAPIV3.PathItemObject>): void {
    this.#methods.set(method, pathItem);
  }

  public process(): SourceFile {
    this.#context.logger.debug(`Processing endpoint: ${this.#name}`);

    const statements = Array.from(this.#methods, ([method, pathItem]) => this.#processMethod(method, pathItem)).flatMap(
      (item) => item,
    );

    const { imports, exports } = this.#dependencies;

    return createSourceFile(
      [...imports.toCode(), ...statements, ...exports.toCode()],
      this.#sourcePathProcessor.process(this.#name),
    );
  }

  #processMethod(method: string, pathItem: ReadonlyDeep<OpenAPIV3.PathItemObject>): readonly Statement[] {
    this.#context.logger.debug(`Processing endpoint method: ${this.#name}.${method}`);

    return Object.values(OpenAPIV3.HttpMethods)
      .filter((httpMethod) => pathItem[httpMethod])
      .map((httpMethod) =>
        EndpointMethodOperationProcessor.createProcessor(
          httpMethod,
          this.#name,
          method,
          pathItem[httpMethod] as EndpointMethodOperation,
          this.#dependencies,
          this.#context,
        )?.process(),
      )
      .filter(Boolean) as readonly Statement[];
  }
}
