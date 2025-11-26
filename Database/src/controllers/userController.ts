import { Request, Response } from "express";
import { BaseController } from "./baseController";
import { UserRepository } from "../repositories/userRepository";
import { Usuario } from "../models";
import { TipoUsuario } from "../types/express";

export class UserController extends BaseController<Usuario> {
    private userRepository: UserRepository;
    constructor() {
        const userRepo = new UserRepository();
        super(userRepo)
        this.userRepository = userRepo;
    }

    async getByEmail(req: Request, res: Response): Promise<any> {
        try {
            const { correo } = req.params;
            
            if (!correo) {
                return res.status(400).json({ message: "Debe proporcionar un correo" });
            }

            if (typeof correo !== "string") {
                return res.status(400).json({ message: "Debe proporcionar un correo válido como parámetro de consulta" });
            }

            const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
            if (!emailRegex.test(correo)) {
                return res.status(400).json({ message: "Formato de correo inválido" });
            }

            const user = await this.userRepository.findByEmail(correo);
            user
                ? res.json(user)
                : res.status(404).json({ error: "Usuario no encontrado" });
        } catch (error) {
            this.handleError(error, res, "Error al obtener usuario por correo");
        }
    }

    async getByActivo(req: Request, res: Response): Promise<any> {
        try {
            const { activo } = req.params
            const activBoolean = activo === "true" ? true : activo === "false" ? false : null;

            if (activBoolean === null) {
                return res.status(400).json({ message: "Activo debe ser true o false" })
            }

            const users = await this.userRepository.findByActivo(activBoolean)

            users.length > 0
                ? res.json(users)
                : res.status(404).json({ error: 'User not found' });
        } catch (error) {
            this.handleError(error, res, "Error getting user by email")
        }
    }

    async getByUserType(req: Request, res: Response): Promise<any> {
        try {
            const { tipoUsuario } = req.params;

            const isValid = Object.values(TipoUsuario).includes(tipoUsuario as unknown as TipoUsuario)

            if (!isValid) {
                return res.status(400).json({ error: "Tipo de usuario inválido" });
            }

            const tipo = tipoUsuario as unknown as TipoUsuario

            const users = await this.userRepository.findByUserType(tipo);
            users.length > 0
                ? res.json(users)
                : res.status(404).json({ error: "No se encontraron usuarios para este tipo" });
        } catch (error) {
            this.handleError(error, res, "Error al obtener usuarios por tipo");
        }
    }

}