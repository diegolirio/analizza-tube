import { render, screen } from "@testing-library/react";
import { expect, test } from "vitest";
import Home from "./page";

test("aTelaInicialConvidaAEscolherOuEnviarUmVideo", () => {
  render(<Home />);
  expect(screen.getByRole("heading", { name: /escolha um vídeo/i })).toBeInTheDocument();
});
